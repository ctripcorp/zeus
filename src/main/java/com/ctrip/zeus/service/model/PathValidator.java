package com.ctrip.zeus.service.model;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.model.common.MetaType;
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

    public LocationEntry checkOverlapRestriction(Long vsId, LocationEntry insertEntry, List<LocationEntry> currentEntrySet) throws ValidationException {
        if (insertEntry == null) {
            throw new NullPointerException("Null location entry value when executing path overlap check.");
        }
        insertEntry.setPath(PathUtils.pathReformat(insertEntry.getPath()));

        if (currentEntrySet == null || currentEntrySet.size() == 0) return insertEntry;


        String insertUri = PathUtils.extractUriIgnoresFirstDelimiter(insertEntry.path);
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

    public static class LocationEntry {
        Long vsId;
        Long entryId;
        MetaType entryType;
        String path;
        Integer priority;

        public Long getVsId() {
            return vsId;
        }

        public LocationEntry setVsId(Long vsId) {
            this.vsId = vsId;
            return this;
        }

        public Long getEntryId() {
            return entryId;
        }

        public LocationEntry setEntryId(Long entryId) {
            this.entryId = entryId;
            return this;
        }

        public MetaType getEntryType() {
            return entryType;
        }

        public LocationEntry setEntryType(MetaType entryType) {
            this.entryType = entryType;
            return this;
        }

        public String getPath() {
            return path;
        }

        public LocationEntry setPath(String path) {
            this.path = path;
            return this;
        }

        public Integer getPriority() {
            return priority;
        }

        public LocationEntry setPriority(Integer priority) {
            this.priority = priority;
            return this;
        }

        @Override
        public String toString() {
            return entryType.toString() + "-" + entryId;
        }
    }

    class Node {
        String first;
        String last;
        List<Node> middle;
    }

    //TODO use graph
    private int buildPathGraph(char[] pathArray, Node parent, int startIdx, int depth) throws ValidationException {
        if (pathArray == null) return -1;
        if (startIdx >= pathArray.length) return startIdx;
        if (depth > 10)
            throw new ValidationException("Too deep recursive sub-paths are found. Path validation rejected.");

        StringBuilder pathBuilder = new StringBuilder();
        Node current = new Node();
        int i = startIdx;

        for (; i < pathArray.length; i++) {
            switch (pathArray[i]) {
                case '(':
                    if (pathBuilder.length() > 0) {
                        current.first = pathBuilder.toString();
                        pathBuilder.setLength(0);
                        i = buildPathGraph(pathArray, current, i, depth + 1);
                    }
                    break;
                case ')':
                    if (pathBuilder.length() > 0) {
                        current.last = pathBuilder.toString();
                        pathBuilder.setLength(0);
                    }
                    parent.middle.add(current);
                    return i;
                case '|':
                    if (current.middle.size() > 0) {
                        throw new ValidationException("Ambiguous regex path is detected. \"path\" : " + new String(pathArray));
                    }
                    if (pathBuilder.length() > 0) {
                        current.first = pathBuilder.toString();
                        pathBuilder.setLength(0);
                        parent.middle.add(current);
                        current = new Node();
                    }
                    break;
                default:
                    pathBuilder.append(pathArray[i]);
                    break;
            }
        }

        if (pathBuilder.length() > 0) {
            current.last = pathBuilder.toString();
            pathBuilder.setLength(0);
        }

        parent.middle.add(current);
        return i;
    }
}
