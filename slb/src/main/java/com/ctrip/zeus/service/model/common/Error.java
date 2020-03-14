package com.ctrip.zeus.service.model.common;

import java.util.*;

/**
 * Created by zhoumy on 2017/2/7.
 */
class Error {
    private final Map<Long, Node> entryIdReference;

    public Error() {
        entryIdReference = new HashMap<>();
    }

    public Set<Long> getErrorIds() {
        Set<Long> res = new HashSet<>();
        for (Long k : entryIdReference.keySet()) {
            Node tmp = entryIdReference.get(k);
            while (tmp != null) {
                if (!tmp.isIgnore()) {
                    res.add(k);
                    break;
                }
                tmp = tmp.getNext();
            }
        }
        return res;
    }

    public Node getErrorMessage(Long groupId) {
        return entryIdReference.get(groupId);
    }

    public Set<String> getErrorTypes(Long entryId) {
        Set<String> result = new HashSet<>();
        Node root = getErrorMessage(entryId);
        if (root == null) return result;
        for (Error.Node n : root.getFlattenNodes()) {
            if (!n.isIgnore()) {
                result.add(n.getErrorType());
            }
        }
        return result;
    }

    public List<Error.Node> getErrorNodes(Long entryId) {
        List<Error.Node> result = new ArrayList<>();
        Node root = getErrorMessage(entryId);
        if (root == null) return result;
        for (Error.Node n : root.getFlattenNodes()) {
            if (!n.isIgnore()) {
                result.add(n);
            }
        }
        return result;
    }

    public int size() {
        return entryIdReference.size();
    }

    public boolean isEmpty() {
        return getErrorIds().size() == 0;
    }

    public Map<Long, String> listErrors() {
        Map<Long, String> result = new HashMap<>(entryIdReference.size());
        for (Long id : getErrorIds()) {
            result.put(id, entryIdReference.get(id).toString());
        }
        return result;
    }

    public void report(Long entryId, String errorType, String cause) {
        report(entryId, null, null, errorType, cause);
    }

    public void report(Long entryId, Long relatedId, MetaType relatedType, String errorType, String cause) {
        Node n = new Node(entryId, relatedId, relatedType, errorType, cause);
        Node prev = entryIdReference.get(entryId);
        if (prev != null) {
            if (prev.errorType.equals(errorType) && relatedId == null && prev.getRelatedId() == null) {
                return;
            }
            prev.add(n);
        } else {
            entryIdReference.put(entryId, n);
        }
    }

    public void ignore(Long entryId, String errorType, MetaType relatedType, Long relatedId) {
        Error.Node node = getErrorMessage(entryId);
        if (node == null) return;
        Error.Node curr = node;
        while (curr != null) {
            if (curr.getErrorType().equals(errorType)) {
                if ((relatedType != null && relatedType.equals(curr.getRelatedType())) ||
                        (relatedType == null && curr.getRelatedType() == null)) {
                    if (relatedId == null && curr.getRelatedId() == null) {
                        curr.setIgnore(true);
                    } else if (relatedId != null && relatedId.equals(curr.getRelatedId())) {
                        curr.setIgnore(true);
                    }
                }
            }
            curr = curr.getNext();
        }
    }

    public void ignore(Long entryId, String errorType) {
        Error.Node node = getErrorMessage(entryId);
        if (node == null) return;
        Error.Node curr = node;
        while (curr != null) {
            if (curr.getErrorType().equals(errorType)) {
                curr.setIgnore(true);
            }
            curr = curr.getNext();
        }
    }


    protected static class Node {
        private int idx;
        private Long entryId;
        private Long relatedId;
        private MetaType relatedType;
        private String errorType;
        private String message;
        private Node next;
        private boolean ignore;

        public Node(Long entryId, Long relatedId, MetaType relatedType, String errorType, String message) {
            this.entryId = entryId;
            this.errorType = errorType;
            this.relatedId = relatedId;
            this.message = message;
            this.relatedType = relatedType;
            this.ignore = false;
            this.idx = 0;
        }

        public Node(Node another) {
            this.entryId = another.getEntryId();
            this.relatedId = another.getRelatedId();
            this.errorType = another.getErrorType();
            this.message = another.getMessage();
            this.relatedType = another.getRelatedType();
            this.ignore = another.isIgnore();
        }

        public void add(Node next) {
            this.next = next;
            next.idx = idx + 1;
        }

        public Node getNext() {
            return next;
        }

        public List<Node> getFlattenNodes() {
            List<Node> result = new ArrayList<>();
            result.add(new Node(this));
            Node n = next;
            while (n != null) {
                result.add(new Node(n));
                n = n.getNext();
            }
            return result;
        }

        public boolean isIgnore() {
            return ignore;
        }

        public void setIgnore(boolean ignore) {
            this.ignore = ignore;
        }

        public MetaType getRelatedType() {
            return relatedType;
        }

        public void setRelatedType(MetaType relatedType) {
            this.relatedType = relatedType;
        }

        public Long getRelatedId() {
            return relatedId;
        }

        public void setRelatedId(Long relatedId) {
            this.relatedId = relatedId;
        }

        public Long getEntryId() {
            return entryId;
        }

        public String getErrorType() {
            return errorType;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {

            StringBuilder value = new StringBuilder();
            if (!ignore) {
                value.append("{ ErrorNo." + idx + " : \"" + errorType + "-" + message + "\" }");
            }
            if (next != null) {
                value.append(" ").append(next.toString());
            }
            return value.toString();
        }
    }
}
