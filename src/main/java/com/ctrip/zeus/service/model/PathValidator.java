package com.ctrip.zeus.service.model;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.model.common.LocationEntry;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.model.grammar.GrammarException;
import com.ctrip.zeus.service.model.grammar.PathParseHandler;
import com.ctrip.zeus.service.model.grammar.PathUtils;
import com.google.common.base.Joiner;
import org.springframework.stereotype.Service;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

/**
 * Created by zhoumy on 2017/1/11.
 */
@Service("pathValidator")
public class PathValidator {
    private static final String standardSuffix = "($|/|\\?)";
    private static final String[] standardSuffixIdentifier = new String[]{"$", "/"};

    private static final String ROOT = "/";
    private PathParseHandler pathParseHandler;

    public PathValidator() {
        pathParseHandler = new PathParseHandler();
    }

    public void checkOverlapRestricition(List<LocationEntry> locationEntries, ValidationContext context) {
        if (locationEntries.size() == 0) return;
        if (context == null) context = new ValidationContext();

        List<Node> _locationEntries = new ArrayList<>(locationEntries.size());
        for (LocationEntry e : locationEntries) {
            Node n = (Node) e;
            analyzeLocationEntry(context, n);
            _locationEntries.add(n);
        }

        int size = _locationEntries.size();
        short[][] visited = new short[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                Node e1 = _locationEntries.get(i);
                Node e2 = _locationEntries.get(j);
                if (e1.getPathValues() == null || e2.getPathValues() == null) {
                    visited[i][j] = -2;
                }
                boolean rendered = false;
                for (String s1 : e1.getPathValues()) {
                    for (String s2 : e2.getPathValues()) {
                        try {
                            rendered = rendered || renderPriority(e1, e2, s1, s2);
                        } catch (ValidationException e) {
                            visited[i][j] = -1;
                            visited[i][i] = -1;
                        }
                    }
                }
                visited[i][j] = rendered ? (short) 1 : (short) 0;
            }
        }

        for (int i = 0; i < size; i++) {
            if (visited[i][i] != -1) {
                _locationEntries.get(i).adjust();
                continue;
            }

            Node e1 = _locationEntries.get(i);
            context.error(e1.getEntryId(), e1.getEntryType(), "Cannot adjust path priority and may cause path overlap.");
            for (int j = i + 1; j < size; j++) {
                if (visited[i][i] + visited[i][j] == 0) {
                    Node e2 = _locationEntries.get(j);
                    context.error(e2.getEntryId(), e2.getEntryType(), "Cannot adjust path priority and may cause path overlap.");
                }
            }
        }
    }

    private void analyzeLocationEntry(ValidationContext context, Node e) {
        String[] v;
        try {
            v = pathParseHandler.parse(e.getPath());
        } catch (GrammarException e1) {
            context.error(e.getEntryId(), e.getEntryType(), e1.getMessage());
            return;
        }
        if (v == null || v.length == 0) {
            context.error(e.getEntryId(), e.getEntryType(), "No valid path.");
        }
        for (String p : v) {
            if (ROOT.equals(p)) {
                if (v.length > 1) {
                    context.error(e.getEntryId(), e.getEntryType(), "Root path cannot share location entries with other patterns.");
                    return;
                } else {
                    e.setRoot(true);
                }
            }
        }
        e.setPathValues(v);
        e.setModifiable(e.getPriority() == null);
        if (!e.isRoot()) {
            e.setPriority(e.getPriority() == null ? 1000 : e.getPriority());
        } else if (e.getPath().equals(ROOT)) {
            e.setPriority(e.getPriority() == null ? -1000 : e.getPriority());
        } else {
            e.setPriority(e.getPriority() == null ? -1100 : e.getPriority());
        }
    }

    private boolean renderPriority(Node e1, Node e2, String s1, String s2) throws ValidationException {
        switch (PathUtils.prefixOverlaps(s1, s2)) {
            case -1:
                return false;
            case 0:
                throw new ValidationException("The two path being compared are completely equivalent.");
            case 1:
                if (!e1.reduceRange(Integer.MIN_VALUE, e2.getPriority())) {
                    if (!e2.reduceRange(e1.getPriority(), Integer.MAX_VALUE)) {
                        throw new ValidationException("Path priority range cannot be reduced anymore.");
                    }
                }
                return true;
            case 2:
                if (!e1.reduceRange(e2.getPriority(), Integer.MAX_VALUE)) {
                    if (!e2.reduceRange(Integer.MIN_VALUE, e2.getPriority())) {
                        throw new RuntimeException("Path priority range cannot be reduced anymore.");
                    }
                }
                return true;
        }
        return false;
    }

    public static class Node extends LocationEntry {
        boolean root;
        boolean modifiable = false;

        String[] pathValues;

        int floor = Integer.MAX_VALUE;
        int ceiling = Integer.MIN_VALUE;

        public void setModifiable(boolean modifiable) {
            this.modifiable = modifiable;
        }

        String[] getPathValues() {
            return pathValues;
        }

        void setPathValues(String[] pathValues) {
            this.pathValues = pathValues;
        }

        boolean isRoot() {
            return root;
        }

        void setRoot(boolean root) {
            this.root = root;
        }

        boolean reduceRange(int floor, int ceiling) {
            if (modifiable) {
                if (floor > this.ceiling || ceiling < this.floor) return false;
                this.floor = this.floor < floor ? floor : this.floor;
                this.ceiling = this.ceiling > ceiling ? ceiling : this.ceiling;
                return true;
            }
            return false;
        }

        void adjust() {
            if (!modifiable) return;

            if (floor < 1000 && ceiling > 1000) {
                setPriority(1000);
                return;
            }

            if (ceiling == Integer.MAX_VALUE) {
                ceiling = floor + 100;
            }
            if (floor == Integer.MIN_VALUE) {
                floor = ceiling - 100;
            }
            setPriority((ceiling + floor) / 2);
        }
    }

    @Deprecated
    public LocationEntry checkOverlapRestriction(Long vsId, LocationEntry insertEntry, List<LocationEntry> currentEntrySet) throws ValidationException {
        if (insertEntry == null) {
            throw new NullPointerException("Null location entry value when executing path overlap check.");
        }
        try {
            insertEntry.setPath(PathUtils.pathReformat(insertEntry.getPath()));
        } catch (GrammarException e) {
            throw new ValidationException(e.getMessage());
        }

        if (currentEntrySet == null || currentEntrySet.size() == 0) return insertEntry;


        String insertUri = PathUtils.extractUriIgnoresFirstDelimiter(insertEntry.getPath());
        boolean insertRootUri = "/".equals(insertUri);
        insertEntry.setPriority(insertEntry.getPriority() == null ? (insertRootUri ? ("/".equals(insertEntry.getPath()) ? -1100 : -1000) : 1000) : insertEntry.getPriority());

        List<String> insertPathMembers = getPathCandidates(insertEntry, insertUri, insertRootUri);

        Set<LocationEntry> overlappedEntries = new HashSet<>();
        for (LocationEntry entryNode : currentEntrySet) {
            if ((insertEntry.getEntryId() != null && insertEntry.getEntryId().equals(entryNode.getEntryId()))
                    && insertEntry.getEntryType().equals(entryNode.getEntryType())) continue;

            String entryNodeUri = entryNode.getPath();
            try {
                entryNodeUri = PathUtils.extractUriIgnoresFirstDelimiter(entryNode.getPath());
            } catch (ValidationException ex) {
            }
            entryNode.setPriority((entryNode.getPriority() != null && entryNode.getPriority().equals(0)) ? null : entryNode.getPriority());
            boolean rootUri = "/".equals(entryNodeUri);

            // compare root uri explicitly
            if (insertRootUri && rootUri) {
                if (entryNode.getPath().equals(insertEntry.getPath())) {
                    overlappedEntries.add(entryNode);
                } else {
                    insertEntry.setPriority("/".equals(entryNode.getPath()) ? entryNode.getPriority() + 100 : entryNode.getPriority() - 100);
                }
                continue;
            }

            List<String> retainedPathMembers = getPathCandidates(entryNode, entryNodeUri, rootUri);
            for (String ap : insertPathMembers) {
                for (String rp : retainedPathMembers) {
                    if (insertRootUri && insertEntry.getPriority() >= entryNode.getPriority()) {
                        insertEntry.setPriority(entryNode.getPriority() - 100);
                        continue;
                    }
                    if (rootUri && insertEntry.getPriority() <= entryNode.getPriority()) {
                        insertEntry.setPriority(entryNode.getPriority() + 100);
                        continue;
                    }

                    int ol = PathUtils.prefixOverlapped(ap, rp, standardSuffix);
                    switch (ol) {
                        case -1:
                            break;
                        case 0:
                            overlappedEntries.add(entryNode);
                            break;
                        case 1:
                            if (insertEntry.getPriority() <= entryNode.getPriority()) {
                                insertEntry.setPriority(entryNode.getPriority() + 100);
                            }
                            break;
                        case 2:
                            if (insertEntry.getPriority() >= entryNode.getPriority()) {
                                insertEntry.setPriority(entryNode.getPriority() - 100);
                            }
                            break;
                        default:
                            throw new NotImplementedException();
                    }
                }
            }
        }

        if (overlappedEntries.size() > 0) {
            List<LocationEntry> entries = new ArrayList<>();
            for (LocationEntry e : overlappedEntries) {
                entries.add(e);
            }
            throw new ValidationException("Path that you tried to add/update is completely equivalent to existing entries: [ " + Joiner.on(",").join(entries) + " at vs " + vsId + ".");
        }
        return insertEntry;
    }

    private List<String> getPathCandidates(LocationEntry locationEntry, String refinedUri, boolean isRootUri) throws ValidationException {
        List<String> pathCandidates = new ArrayList<>();
        if (!isRootUri) {
            locationEntry.setPriority(locationEntry.getPriority() == null ? 1000 : locationEntry.getPriority());
            pathCandidates = splitParallelPaths(refinedUri, 1);
            if (pathCandidates.size() == 0) pathCandidates.add(refinedUri);
        } else {
            boolean exactRootUri = "/".equals(locationEntry.getPath());
            locationEntry.setPriority(locationEntry.getPriority() == null ? (exactRootUri ? -1100 : -1000) : locationEntry.getPriority());
            pathCandidates.add(refinedUri);
        }
        return pathCandidates;
    }

    @Deprecated
    public List<String> splitParallelPaths(String path, int depth) throws ValidationException {
        List<String> pathMembers = new ArrayList<>();
        if (depth > 1) {
            throw new IllegalArgumentException("SplitParallelPaths only supports splitting regex path at first level.");
        }
        int fromIdx, idxSuffix;
        fromIdx = idxSuffix = 0;
        while ((idxSuffix = path.indexOf(standardSuffix, fromIdx)) != -1) {
            if (fromIdx > 0) {
                if (path.charAt(fromIdx) == '|') {
                    fromIdx++;
                    pathMembers.addAll(splitParallelPaths(path.substring(fromIdx, idxSuffix), true));
                } else {
                    String prev = pathMembers.get(pathMembers.size() - 1);

                    List<String> subPaths = splitParallelPaths(prev + path.substring(fromIdx, idxSuffix + 8), true);
                    pathMembers.set(pathMembers.size() - 1, subPaths.get(0));
                    for (int i = 1; i < subPaths.size(); i++) {
                        pathMembers.add(pathMembers.get(i));
                    }
                }
            } else {
                pathMembers.addAll(splitParallelPaths(path.substring(0, idxSuffix), true));
            }
            fromIdx = idxSuffix + 8;
        }

        if (pathMembers.size() == 0) {
            pathMembers.addAll(splitParallelPaths(path, false));
        }

        return pathMembers;
    }

    private List<String> splitParallelPaths(String path, boolean appendSuffix) throws ValidationException {
        if (path == null || path.isEmpty()) return null;

        List<String> subPaths = new ArrayList<>();
        StringBuilder pathBuilder = new StringBuilder();
        char[] pp = path.toCharArray();
        int startIdx, endIdx;
        startIdx = 0;
        endIdx = pp.length - 1;
        if (pp[startIdx] == '(' && pp[endIdx] == ')') {
            startIdx++;
            endIdx--;
        }

        for (int i = startIdx; i <= endIdx; i++) {
            switch (pp[i]) {
                case '|':
                    if (appendSuffix) {
                        String p = pathBuilder.toString();
                        pathBuilder.setLength(0);
                        for (String s : standardSuffixIdentifier) {
                            subPaths.add(p + s);
                        }
                    }
                    break;
                default:
                    pathBuilder.append(pp[i]);
                    break;
            }
        }

        if (pathBuilder.length() > 0) {
            String p = pathBuilder.toString();
            pathBuilder.setLength(0);
            if (appendSuffix) {
                for (String s : standardSuffixIdentifier) {
                    subPaths.add(p + s);
                }
            } else {
                subPaths.add(p);
            }
        }

        if (subPaths.size() == 0) {
            for (String s : standardSuffixIdentifier) {
                subPaths.add(path + s);
            }
        }

        return subPaths;
    }
}
