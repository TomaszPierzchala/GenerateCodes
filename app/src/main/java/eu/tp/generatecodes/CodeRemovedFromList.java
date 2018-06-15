package eu.tp.generatecodes;

enum CodeLists {
    TO_BE_CHECKED {
        @Override
        public String toString() {
            return "To Be Checked list";
        }
    },
    ALREADY_CHECKED {
        @Override
        public String toString() {
            return "Already Checked list";
        }
    }, BOTH {
        @Override
        public String toString() {
            return "BOTH To Be AND Already Checked lists";
        }
    }
}

public class CodeRemovedFromList {
    private String oldCode;
    private CodeLists removedFromCodeList;

    private Boolean success = null;

    public String getOldCode() {
        return oldCode;
    }

    public CodeLists getRemovedFromCodeList() {
        return removedFromCodeList;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Boolean getSuccess() {
        return success;
    }

    CodeRemovedFromList(Boolean success) {
        this.oldCode = null;
        this.removedFromCodeList = null;
        this.success = success;
    }

    CodeRemovedFromList(String oldCode, CodeLists codeList) {

        this.oldCode = oldCode;
        this.removedFromCodeList = codeList;
    }
}
