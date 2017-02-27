package com.ctrip.zeus.service.model.validation;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.model.common.LocationEntry;
import com.ctrip.zeus.service.model.common.MetaType;
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
    private static final Logger logger = LoggerFactory.getLogger(PathValidator.class);

    private static final String ROOT = "/";
    private static final String ERROR_TYPE = "PATH_VALIDATION";
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
        for (int i = size - 1; i >= 0; i--) {
            for (int j = i - 1; j >= 0; j--) {
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
                throw new ValidationException("The two path being compared are completely equivalent. Unable to solve the conflict. " + e1.toString() + " and " + e2.toString());
            case 1:
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
                } else if (e.getPath().equals(ROOT)) {
                    priority = -2000;
                } else {
                    priority = -1000;
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
