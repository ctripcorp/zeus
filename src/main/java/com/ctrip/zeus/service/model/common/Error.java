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
        return new HashSet<>(entryIdReference.keySet());
    }

    public Node getErrorMessage(Long groupId) {
        return entryIdReference.get(groupId);
    }

    public int size() {
        return entryIdReference.size();
    }

    public Map<Long, String> listErrors() {
        Map<Long, String> result = new HashMap<>(entryIdReference.size());
        for (Map.Entry<Long, Node> e : entryIdReference.entrySet()) {
            result.put(e.getKey(), e.getValue().toString());
        }
        return result;
    }

    public void report(Long entryId, String errorType, String cause) {
        Node n = new Node(entryId, errorType, cause);
        Node prev = entryIdReference.get(entryId);
        if (prev != null) {
            if (prev.errorType.equals(errorType)) {
                return;
            }
            prev.add(n);
        } else {
            entryIdReference.put(entryId, n);
        }
    }

    protected static class Node {
        private int idx;
        private Long entryId;
        private String errorType;
        private String message;
        private Node next;

        public Node(Long entryId, String errorType, String message) {
            this.entryId = entryId;
            this.errorType = errorType;
            this.message = message;
            this.idx = 0;
        }

        public Node(Node another) {
            this.entryId = another.getEntryId();
            this.errorType = another.getErrorType();
            this.message = another.getMessage();
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
            Node n = getNext();
            while (n != null) {
                result.add(new Node(n));
            }
            return result;
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
            StringBuilder value = new StringBuilder().append("{ ErrorNo." + idx + " : \"" + errorType + "-" + message + "\" }");
            if (next != null) {
                value.append(", ").append(next.toString());
            }
            return value.toString();
        }
    }
}
