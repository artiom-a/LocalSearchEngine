package club.dagomys.siteparcer.src.entity.response;

public abstract class Response {
    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    private boolean result;


}
