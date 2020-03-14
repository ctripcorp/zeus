package com.ctrip.zeus.service.model.validation;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.model.common.ErrorType;
import com.ctrip.zeus.service.model.common.LocationEntry;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.model.grammar.GrammarException;
import com.ctrip.zeus.service.model.grammar.PathParseHandler;
import com.ctrip.zeus.service.model.grammar.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zhoumy on 2017/1/11.
 */
@Service("pathValidator")
public class PathValidator {
    private static final Logger logger = LoggerFactory.getLogger(PathValidator.class);

    private static final String ROOT = "/";
    private PathParseHandler pathParseHandler;

    public PathValidator() {
        pathParseHandler = new PathParseHandler();
    }

    /**
     * Check if path1 contains path2
     *
     * @param path1
     * @param path2
     * @return true if contains
     */
    public boolean contains(String path1, String path2) {
        try {
            String[] checkParts = pathParseHandler.parse(path2);
            String[] unionParts = pathParseHandler.parse(path1);

            boolean[] flag = new boolean[checkParts.length];
            for (int i = 0; i < checkParts.length; i++) {
                flag[i] = false;
                for (String p : unionParts) {
                    int ol = PathUtils.prefixOverlaps(checkParts[i], p);
                    if (ol == 0 || ol == 1) {
                        flag[i] = true;
                        break;
                    }
                }
            }
            boolean result = true;
            for (int i = 0; i < flag.length; i++) {
                result &= flag[i];
            }
            return result;
        } catch (GrammarException e) {
            return false;
        }
    }

    public void checkPathOverlapAsError(List<LocationEntry> entryList, ValidationContext context) {
        if (entryList == null || entryList.size() <= 1) return;
        List<LocationEntry> locationEntries = new ArrayList<>();
        for (LocationEntry e : entryList) {
            // No path, no overlap.
            if (e.getPath() == null || e.getPath().isEmpty()) {
                continue;
            }
            if (e.getEntryType().equals(MetaType.GROUP)) {
                locationEntries.add(e);
            }
        }
        Collections.sort(locationEntries, new Comparator<LocationEntry>() {
            @Override
            public int compare(LocationEntry o1, LocationEntry o2) {
                int result = o2.getPriority() - o1.getPriority();
                return result == 0 ? o2.getEntryId().compareTo(o1.getEntryId()) : result;
            }
        });
        for (int i = 0; i < locationEntries.size(); i++) {
            LocationEntry source = locationEntries.get(i);
            for (int j = i + 1; j < locationEntries.size(); j++) {
                LocationEntry target = locationEntries.get(j);
                try {
                    Node node = new Node(pathParseHandler, target);
                    if (node.isRoot()) {
                        context.error(source.getEntryId(), source.getEntryType(),
                                target.getEntryId(), target.getEntryType(), ErrorType.ROOT_PATH_OVERLAP,
                                target.toString() + "'s Path Contained By " + target.toString());
                        break;
                    }
                } catch (Exception e) {
                    logger.warn("Parser Node Failed." + target.toString(), e);
                }
                if (contains(target.getPath(), source.getPath())) {
                    context.error(source.getEntryId(), source.getEntryType(),
                            target.getEntryId(), target.getEntryType(), ErrorType.PATH_OVERLAP,
                            source.toString() + "'s Path Contained By " + target.toString());
                    break;
                }
            }
        }
    }

    public void checkOverlapRestricition(List<LocationEntry> locationEntries, ValidationContext context) {
        if (locationEntries.size() == 0) return;
        if (context == null) context = new ValidationContext();

        List<Node> entries = new ArrayList<>(locationEntries.size());
        for (LocationEntry e : locationEntries) {
            // No path, no overlap.
            if (e.getPath() == null || e.getPath().isEmpty()) {
                continue;
            }
            try {
                Node n = new Node(pathParseHandler, e);
                entries.add(n);
            } catch (Exception e1) {
                context.error(e.getEntryId(), e.getEntryType(), ErrorType.PATH_VALIDATION, e1.getMessage());
            }
        }

        int size = entries.size();
        boolean[] visited = new boolean[size];
        boolean[] delayCompare = new boolean[size];
        for (int i = size - 1; i >= 0; i--) {
            for (int j = i - 1; j >= 0; j--) {
                if (!compareAndModify(context, visited, entries, i, j, true)) {
                    delayCompare[i] = true;
                    delayCompare[j] = true;
                }
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if (delayCompare[i] && delayCompare[j]) {
                    delayCompare[j] = false;
                    Node e1 = entries.get(i);
                    try {
                        e1.adjustByRange(300);
                    } catch (ValidationException e) {
                        logger.error("Unexpected validation exception.", e);
                    }
                    compareAndModify(context, visited, entries, i, j, false);
                }
            }
        }

        for (int i = 0; i < size; i++) {
            if (!visited[i]) {
                try {
                    entries.get(i).adjust();
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

        // Both entries are modifiable, and we can delay the comparison,
        // so we shall get back later when priority ranges are adjusted based on non-modifiable entries.
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
                    context.error(e1.value.getEntryId(), e1.value.getEntryType(), e2.value.getEntryId(), e2.value.getEntryType(), ErrorType.PATH_VALIDATION, e.getMessage());
                    context.error(e2.value.getEntryId(), e2.value.getEntryType(), e1.value.getEntryId(), e1.value.getEntryType(), ErrorType.PATH_VALIDATION, e.getMessage());
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
                context.error(e1.value.getEntryId(), e1.value.getEntryType(), e2.value.getEntryId(), e2.value.getEntryType(), ErrorType.PATH_VALIDATION, e.getMessage());
                if (!delayEnabled) {
                    visited[j] = true;
                    context.error(e2.value.getEntryId(), e2.value.getEntryType(), e1.value.getEntryId(), e1.value.getEntryType(), ErrorType.PATH_VALIDATION, e.getMessage());
                    return true;
                }
            }

            try {
                e2.verifyRange();
            } catch (ValidationException e) {
                visited[j] = true;
                context.error(e2.value.getEntryId(), e2.value.getEntryType(), e1.value.getEntryId(), e1.value.getEntryType(), ErrorType.PATH_VALIDATION, e.getMessage());
                if (!delayEnabled) {
                    visited[i] = true;
                    context.error(e1.value.getEntryId(), e1.value.getEntryType(), e2.value.getEntryId(), e2.value.getEntryType(), ErrorType.PATH_VALIDATION, e.getMessage());
                }
            }
        }
        return true;
    }

    private boolean renderPriority(Node e1, Node e2, String s1, String s2) throws ValidationException {
        switch (PathUtils.prefixOverlaps(s1, s2)) {
            case -1:
                // No overlap
                return false;
            case 0:
                // Equivalent
                throw new ValidationException("The two path being compared are completely equivalent. Unable to solve the conflict. " + e1.toString() + " and " + e2.toString());
            case 1:
                // e2 contains e1 (e2 is the parent of e1), so we need to ensure that e2.priority < e1.priority.
                if (!e1.reduceRange(e2.getPriority(), Integer.MAX_VALUE)) {
                    if (!e2.reduceRange(Integer.MIN_VALUE, e1.getPriority())) {
                        if (e2.getPriority() < e1.getPriority()) return false;
                        int p1 = e2.getPriority() + 100;
                        int p2 = e1.getPriority() - 100;
                        p1 = e1.isRoot() ? (p1 < -1000 ? -1000 : p1) : (p1 < 1000 ? 1000 : p1);
                        p2 = e2.isRoot() ? (p2 > -1000 ? -1000 : p2) : (p2 > 1000 ? 1000 : p2);
                        throw new ValidationException("Path priority range cannot be reduced. Modify priority of entry (" + e1.value.getEntryId() + ", " + s1 + ") to " + p1 + " or entry (" + e2.value.getEntryId() + ", " + s2 + ") to " + p2 + ".");
                    }
                }
                return true;
            case 2:
                // e1 contains e2 (e1 is the parent of e2), so we need to ensure that e2.priority > e1.priority.
                if (!e1.reduceRange(Integer.MIN_VALUE, e2.getPriority())) {
                    if (!e2.reduceRange(e1.getPriority(), Integer.MAX_VALUE)) {
                        if (e2.getPriority() > e1.getPriority()) return false;
                        int p1 = e2.getPriority() - 100;
                        int p2 = e1.getPriority() + 100;
                        p1 = e1.isRoot() ? (p1 > -1000 ? -1000 : p1) : (p1 > 1000 ? 1000 : p1);
                        p2 = e2.isRoot() ? (p2 < -1000 ? -1000 : p2) : (p2 < 1000 ? 1000 : p2);
                        throw new ValidationException("Path priority range cannot be reduced. Modify priority of entry (" + e1.value.getEntryId() + ", " + s1 + ") to " + p1 + " or entry (" + e2.value.getEntryId() + ", " + s2 + ") to " + p2 + ".");
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

        // INCLUSIVE floor and ceiling values for priority adjustments.
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

        final void analyzeLocationEntry(PathParseHandler pph, LocationEntry e) throws GrammarException, ValidationException {
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
                } else if (e.getPath().equals(ROOT)) {
                    priority = -2000;
                    e.setPriority(priority);
                    modifiable = false;
                } else {
                    priority = -1000;
                    e.setPriority(priority);
                    modifiable = false;
                }
            } else {
                ceiling = floor = priority = e.getPriority();
            }
        }

        @Override
        public String toString() {
            return value.getEntryType() + "-" + value.getEntryId() + " : (" + value.getPriority() + ", " + value.getPath() + ")";
        }
    }
}
