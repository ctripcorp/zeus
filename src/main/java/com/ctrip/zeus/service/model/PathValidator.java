package com.ctrip.zeus.service.model;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.util.PathUtils;
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
    private static final String[] standardSuffixIdentifier = new String[]{"$", "/", "\\?"};

    public LocationEntry checkOverlapRestriction(Long vsId, LocationEntry addingEntry, List<LocationEntry> retainedEntries) throws ValidationException {
        if (addingEntry == null) {
            throw new NullPointerException("Null location entry value when executing path overlap check.");
        }
        addingEntry.setPath(PathUtils.pathReformat(addingEntry.getPath()));

        if (retainedEntries == null || retainedEntries.size() == 0) return addingEntry;

        String addingUri = PathUtils.extractUriIgnoresFirstDelimiter(addingEntry.path);

        boolean addingRootUri = "/".equals(addingUri);

        List<String> addingPathMembers = new ArrayList<>();
        if (!addingRootUri) {
            addingEntry.setPriority(addingEntry.getPriority() == null ? 1000 : addingEntry.getPriority());
            addingPathMembers = splitParallelPaths(addingUri, 1);
            if (addingPathMembers.size() == 0) addingPathMembers.add(addingUri);
        } else {
            boolean exactRootUri = "/".equals(addingEntry.getPath());
            addingEntry.setPriority(addingEntry.getPriority() == null ? (exactRootUri ? -1100 : -1000) : addingEntry.getPriority());
            addingPathMembers.add(addingUri);
        }

        Set<LocationEntry> overlappedEntries = new HashSet<>();
        for (LocationEntry retainedEntry : retainedEntries) {
            if (addingEntry.getEntryId().equals(retainedEntry.getEntryId())) continue;
            if (retainedEntry.getPriority() == null || retainedEntry.getPriority().equals(0)) {
                retainedEntry.setPriority(1000);
            }

            String retainedUri = retainedEntry.getPath();
            try {
                retainedUri = PathUtils.extractUriIgnoresFirstDelimiter(retainedEntry.getPath());
            } catch (ValidationException ex) {
            }

            boolean retainedRootUri = "/".equals(retainedUri);
            List<String> retainedPathMembers = new ArrayList<>();
            if (!retainedRootUri) {
                retainedPathMembers = splitParallelPaths(retainedUri, 1);
                if (retainedPathMembers.size() == 0) retainedPathMembers.add(retainedUri);
            } else {
                retainedPathMembers.add(retainedUri);
            }

            // check if root path is completely equivalent, otherwise escape comparing with root path
            if (addingRootUri && retainedRootUri) {
                if (retainedEntry.getPath().equals(addingEntry.getPath())) {
                    overlappedEntries.add(retainedEntry);
                }
                continue;
            }

            for (String ap : addingPathMembers) {
                for (String rp : retainedPathMembers) {
                    if (addingRootUri && addingEntry.getPriority() >= retainedEntry.getPriority()) {
                        addingEntry.setPriority(retainedEntry.getPriority() - 100);
                        continue;
                    }
                    if (retainedRootUri && addingEntry.getPriority() <= retainedEntry.getPriority()) {
                        addingEntry.setPriority(retainedEntry.getPriority() + 100);
                        continue;
                    }

                    int ol = PathUtils.prefixOverlapped(ap, rp, standardSuffix);
                    switch (ol) {
                        case -1:
                            break;
                        case 0:
                            overlappedEntries.add(retainedEntry);
                            break;
                        case 1:
                            if (addingEntry.getPriority() <= retainedEntry.getPriority()) {
                                addingEntry.setPriority(retainedEntry.getPriority() + 100);
                            }
                            break;
                        case 2:
                            if (addingEntry.getPriority() >= retainedEntry.getPriority()) {
                                addingEntry.setPriority(retainedEntry.getPriority() - 100);
                            }
                            break;
                        default:
                            throw new NotImplementedException();
                    }
                }
            }
        }

        if (overlappedEntries.size() > 0) {
            List<Long> entryIds = new ArrayList<>();
            for (LocationEntry e : overlappedEntries) {
                entryIds.add(e.getEntryId());
            }
            throw new ValidationException("Path that you tried to add is prefix-overlapped over existing entries: [ " + Joiner.on(",").join(entryIds) + " at vs " + vsId + ".");
        }
        return addingEntry;
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
