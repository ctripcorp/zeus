package com.ctrip.zeus.service.model.grammar;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhoumy on 2017/1/24.
 */
public class PathParseHandler {
    private static final char[] StandardSuffixPattern = "($|/|\\?)".toCharArray();
    private static final List<String> StandardSuffixIdentifier = Arrays.asList("$", "/");

    private LoadingCache<char[], String[]> pathLookupCache = CacheBuilder.newBuilder()
            .maximumSize(5000)
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build(new CacheLoader<char[], String[]>() {
                @Override
                public String[] load(char[] key) throws GrammarException {
                    return parse(key);
                }
            });


    public String[] parse(String path) throws GrammarException {
        try {
            return pathLookupCache.get(extractUriIgnoresFirstDelimiter(PathUtils.pathReformat(path).toCharArray()));
        } catch (ExecutionException e) {
            if (e.getCause() instanceof GrammarException) {
                throw (GrammarException) e.getCause();
            } else {
                throw new GrammarException(e.getCause());
            }
        }
    }

    public String[] parse(char[] path) throws GrammarException {
        List<String> root = new ArrayList<>();
        enumeratePathValues(path, root, 0, 0, '\0');
        return root.toArray(new String[root.size()]);
    }

    public static char[] extractUriIgnoresFirstDelimiter(char[] path) throws GrammarException {
        int idxPrefix = 0;
        int idxModifier = 0;
        boolean quote = false;

        for (char c : path) {
            if (c == '"') {
                quote = true;
                idxPrefix++;
            } else if (c == ' ') {
                idxPrefix++;
                idxModifier = idxPrefix;
            } else if (c == '^' || c == '~' || c == '=' || c == '*') {
                idxPrefix++;
            } else if (c == '/') {
                idxPrefix++;
                if (!quote && idxPrefix < path.length && path[idxPrefix] == '"') {
                    quote = true;
                    idxPrefix++;
                }
                break;
            } else {
                break;
            }
        }

        if (quote && path[path.length - 1] != '\"') {
            throw new GrammarException("Missing end quote. " + "\"path\" : \"" + path + "\"");
        }
        int idxSuffix = quote ? path.length - 1 : path.length;
        if (idxPrefix == idxSuffix) {
            if (path[idxSuffix - 1] == '/') {
                return new char[]{'/'};
            } else {
                throw new GrammarException("Invalid uri after extraction. " + "\"path\" : \"" + path + "\"");
            }
        }
        idxPrefix = idxPrefix < idxSuffix ?
                (idxModifier > idxPrefix ? idxModifier : idxPrefix) : idxModifier;
        return Arrays.copyOfRange(path, idxPrefix, idxSuffix);
    }

    private int enumeratePathValues(char[] pathArray, List<String> prefix, int start, int depth, char startSymbol) throws GrammarException {
        StringBuilder pathBuilder = new StringBuilder();
        int i = start;

        if (depth < 0) {
            throw new GrammarException("Invalid depth " + depth + " during path parsing.");
        }
        if (depth > 20) {
            throw new RuntimeException("Unsupported depth " + depth + " for path parsing.");
        }

        boolean escaped = false;
        for (; i < pathArray.length; i++) {
            if (escaped) {
                pathBuilder.append('\\').append(pathArray[i]);
                escaped = false;
                continue;
            }

            switch (pathArray[i]) {
                case '(': {
                    List<String> subRoot;
                    if (Arrays.equals(StandardSuffixPattern, Arrays.copyOfRange(pathArray, i, i + 8))) {
                        subRoot = StandardSuffixIdentifier;
                        i = i + 7;
                    } else {
                        subRoot = new ArrayList<>();
                        i = enumeratePathValues(pathArray, subRoot, i + 1, depth + 1, '(');
                    }

                    String v = pathBuilder.toString();
                    pathBuilder.setLength(0);
                    if (prefix.size() == 0) {
                        for (String s : subRoot) {
                            prefix.add(v + s);
                        }
                    } else {
                        int psize = prefix.size();
                        for (int j = 1; j < subRoot.size(); j++) {
                            for (int k = 0; k < psize; k++) {
                                prefix.add(prefix.get(k) + v + subRoot.get(j));
                            }
                        }
                        for (int j = 0; j == 0 && subRoot.size() > 0; j++) {
                            for (int k = 0; k < psize; k++) {
                                prefix.set(k, prefix.get(k) + v + subRoot.get(j));
                            }
                        }
                    }
                }
                break;
                case ')':
                    if (pathBuilder.length() > 0) {
                        String v = pathBuilder.toString();
                        pathBuilder.setLength(0);
                        if (prefix.size() == 0) {
                            prefix.add(v);
                        } else {
                            for (int j = 0; j < prefix.size(); j++) {
                                prefix.set(j, prefix.get(j) + v);
                            }
                        }
                    }
                    if (startSymbol == '\0') {
                        throw new GrammarException("Missing left parentheses '(' when parsing to " + new String(pathArray, 0, i) + ".");
                    }
                    return i;
                case '|':
                    if (pathBuilder.length() > 0) {
                        String v = pathBuilder.toString();
                        pathBuilder.setLength(0);

                        if (prefix.size() == 0) {
                            prefix.add(v);
                        } else {
                            for (int j = 0; j < prefix.size(); j++) {
                                prefix.set(j, prefix.get(j) + v);
                            }
                        }
                    }

                    if (startSymbol == '|') {
                        return i;
                    } else {
                        List<String> subRoot = new ArrayList<>();
                        i = enumeratePathValues(pathArray, subRoot, i + 1, depth + 1, '|');
                        for (String s : subRoot) {
                            prefix.add(s);
                        }
                        i--;
                    }
                    break;
                case '?':
                case '*':
                case '[':
                case ']':
                case '{':
                case '}':
                    throw new GrammarException("Character \"" + pathArray[i] + "\" is not allowed in path.");
                case '\\':
                    escaped = !escaped;
                    break;
                default:
                    pathBuilder.append(pathArray[i]);
                    break;
            }
        }

        if (depth > 0 && startSymbol != '|') {
            throw new GrammarException("Unexpected end of path: invalid depth " + depth + " for end symbol " + startSymbol + ".");
        }

        if (pathBuilder.length() > 0) {
            String v = pathBuilder.toString();
            pathBuilder.setLength(0);
            if (prefix.size() == 0) {
                prefix.add(v);
            } else {
                for (int j = 0; j < prefix.size(); j++) {
                    prefix.set(j, prefix.get(j) + v);
                }
            }
        }
        return i;
    }
}