package mx.sekura.sepomex.models;

public class Address {
    public String township;
    public String municipality;
    public String state;
    public String code;

    public Address(String code, String township, String municipality, String state) {
        this.code = code;
        this.township = township;
        this.municipality = municipality;
        this.state = state;
    }
}
