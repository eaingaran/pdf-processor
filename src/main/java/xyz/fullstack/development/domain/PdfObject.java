package xyz.fullstack.development.domain;

public class PdfObject {

    private String id;
    private String qrCodePath;

    public PdfObject() {

    }
    public PdfObject(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQrCodePath() {
        return qrCodePath;
    }

    public void setQrCodePath(String qrCodePath) {
        this.qrCodePath = qrCodePath;
    }
}
