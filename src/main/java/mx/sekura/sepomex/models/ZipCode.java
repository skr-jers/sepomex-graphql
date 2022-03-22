package mx.sekura.sepomex.models;

import java.util.List;

public class ZipCode {
    public String code;
    public List<Address> addresses;

    public ZipCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }
}
