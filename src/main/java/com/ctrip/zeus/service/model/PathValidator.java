package com.ctrip.zeus.service.model;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.model.common.LocationEntry;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.model.grammar.GrammarException;
import com.ctrip.zeus.service.model.grammar.PathParseHandler;
import com.ctrip.zeus.service.model.grammar.PathUtils;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(PathValidator.class);

    private static final String ROOT = "/";
    private static final String ERROR_TYPE = "PATH_VALIDATION";
    private PathParseHandler pathParseHandler;

    public PathValidator() {
        pathParseHandler = new PathParseHandler();
    }

    public void checkOverlapRestricition(List<LocationEntry> locationEntries, ValidationContext context) {
        if (locationEntries.size() == 0) return;
        if (context == null) context = new ValidationContext();

        List<Node> _locationEntries = new ArrayList<>(locationEntries.size());
        for (LocationEntry e : locationEntries) {
            try {
                Node n = new Node(pathParseHandler, e);
                _locationEntries.add(n);
            } catch (Exception e1) {
                context.error(e.getEntryId(), e.getEntryType(), ERROR_TYPE, e1.getMessage());
            }
        }

        int size = _locationEntries.size();
        boolean[] visited = new boolean[size];
        boolean[] delayCompare = new boolean[size];
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if (!compareAndModify(context, visited, _locationEntries, i, j, true)) {
                    delayCompare[i] = true;
                    delayCompare[j] = true;
                }
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if (delayCompare[i] && delayCompare[j]) {
                    delayCompare[j] = false;
                    Node e1 = _locationEntries.get(i);
                    try {
                        e1.adjustByRange(300);
                    } catch (ValidationException e) {
                        logger.error("Unexpected validation exception.", e);
                    }
                    compareAndModify(context, visited, _locationEntries, i, j, false);
                }
            }
        }

        for (int i = 0; i < size; i++) {
            if (!visited[i]) {
                try {
                    _locationEntries.get(i).adjust();
                } catch (ValidationException e) {
                    logger.error("Unexpected validation exception.", e);
                }
            }
        }
    }

    private boolean compareAndModify(ValidationContext context, boolean[] visited,
                                     List<Node> nodes, int i, int j, boolean delayEnabled) {
        Node e1 = nodes.get(i);
        Node e2 = nodes.get(j);
        if (delayEnabled && e1.isModifiable() && e2.isModifiable()) {
            return false;
        }

        boolean rendered = false;
        for (String s1 : e1.getPathValues()) {
            if (visited[i]) break;
            for (String s2 : e2.getPathValues()) {
                try {
                    rendered = rendered || renderPriority(e1, e2, s1, s2);
                } catch (ValidationException e) {
                    visited[i] = true;
                    visited[j] = true;
                    context.error(e1.value.getEntryId(), e1.value.getEntryType(), ERROR_TYPE, e.getMessage());
                    context.error(e2.value.getEntryId(), e2.value.getEntryType(), ERROR_TYPE, e.getMessage());
                    rendered = false;
                    break;
                }
            }
        }
        if (rendered) {
            try {
                e1.verifyRange();
            } catch (ValidationException e) {
                visited[i] = true;
                context.error(e1.value.getEntryId(), e1.value.getEntryType(), ERROR_TYPE, e.getMessage());
                if (!delayEnabled) {
                    visited[j] = true;
                    context.error(e2.value.getEntryId(), e2.value.getEntryType(), ERROR_TYPE, e.getMessage());
                    return true;
                }
            }

            try {
                e2.verifyRange();
            } catch (ValidationException e) {
                visited[j] = true;
                context.error(e2.value.getEntryId(), e2.value.getEntryType(), ERROR_TYPE, e.getMessage());
                if (!delayEnabled) {
                    visited[i] = true;
                    context.error(e1.value.getEntryId(), e1.value.getEntryType(), ERROR_TYPE, e.getMessage());
                }
            }
        }
        return true;
    }

    private boolean renderPriority(Node e1, Node e2, String s1, String s2) throws ValidationException {
        switch (PathUtils.prefixOverlaps(s1, s2)) {
            case -1:
                return false;
            case 0:
                throw new ValidationException("The two path being compared are completely equivalent. `path`=" + s1);
            case 1:
                if (!e1.reduceRange(e2.getPriority(), Integer.MAX_VALUE)) {
                    if (!e2.reduceRange(Integer.MIN_VALUE, e1.getPriority())) {
                        if (e2.getPriority() < e1.getPriority()) return false;
                        throw new ValidationException("Path priority range cannot be reduced.");
                    }
                }
                return true;
            case 2:
                if (!e1.reduceRange(Integer.MIN_VALUE, e2.getPriority())) {
                    if (!e2.reduceRange(e1.getPriority(), Integer.MAX_VALUE)) {
                        if (e2.getPriority() > e1.getPriority()) return false;
                        throw new ValidationException("Path priority range cannot be reduced.");
                    }
                }
                return true;
            default:
                throw new ValidationException("Overlap index is not recognized.");
        }
    }

    public class Node {
        boolean root;
        boolean modifiable = false;

        final LocationEntry value;

        String[] pathValues;

        int floor = Integer.MIN_VALUE;
        int ceiling = Integer.MAX_VALUE;

        int priority;

        public Node(PathParseHandler pph, LocationEntry value) throws GrammarException, ValidationException {
            analyzeLocationEntry(pph, value);
            this.value = value;
        }

        int getPriority() {
            return priority;
        }

        String[] getPathValues() {
            return pathValues;
        }

        boolean isModifiable() {
            return modifiable;
        }

        boolean isRoot() {
            return root;
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

        void adjust() throws ValidationException {
            if (!modifiable) return;

            if (floor < 1000 && ceiling > 1000) {
                priority = 1000;
                value.setPriority(priority);
                return;
            }

            if (ceiling == Integer.MAX_VALUE) {
                ceiling = floor + 200;
            }
            if (floor == Integer.MIN_VALUE) {
                floor = ceiling - 200;
            }
            priority = (ceiling + floor) / 2;
            value.setPriority(priority);
            modifiable = false;
        }

        void adjustByRange(int range) throws ValidationException {
            if (!modifiable) return;

            if (floor < 1000 && ceiling > 1000) {
                priority = 1000 + range;
                value.setPriority(priority);
                return;
            }

            if (ceiling == Integer.MAX_VALUE) {
                ceiling = floor + range * 2;
            }
            if (floor == Integer.MIN_VALUE) {
                floor = ceiling - range * 2;
            }
            priority = (ceiling + floor) / 2;
            value.setPriority(priority);
            modifiable = false;
        }

        void verifyRange() throws ValidationException {
            if (modifiable && ceiling != Integer.MAX_VALUE && floor != Integer.MIN_VALUE && ceiling - floor <= 1)
                throw new ValidationException("Too narrow priority range: (" + floor + "," + ceiling + ").");
        }

        void analyzeLocationEntry(PathParseHandler pph, LocationEntry e) throws GrammarException, ValidationException {
            String[] v;
            try {
                v = pph.parse(e.getPath());
            } catch (GrammarException e1) {
                throw e1;
            }
            if (v == null || v.length == 0) {
                throw new ValidationException(e.toString() + " has no valid path.");
            }
            for (String p : v) {
                if (ROOT.equals(p)) {
                    if (v.length > 1) {
                        throw new ValidationException("Root path cannot share location entries with other patterns.");
                    } else {
                        root = true;
                    }
                }
            }
            pathValues = v;
            modifiable = e.getPriority() == null;
            if (modifiable) {
                if (!root) {
                    priority = 1000;
                } else if (value.getPath().equals(ROOT)) {
                    priority = -1100;
                } else {
                    priority = -1000;
                }
            } else {
                ceiling = floor = priority = e.getPriority();
            }
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

    @Deprecated
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

    @Deprecated
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
