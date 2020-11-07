package Client;

public enum SystemCommands {
    exit ("/exit"), auth ("/auth"), authok ("/auth_ok"), write ("/w"),
    clients ("/clients"), register ("/reg"), registrOK ("/regok"), registrNO ("/regno"),
    timeout("/timeout"), changeNick("/cn"), deleteUsers ("/deleteusers");


    public String getCode() {
        return code;
    }

    private String code;

    SystemCommands(String code) {
        this.code = code;
    }
}