package com.techbotbuilder.streamteamohio.forms;


//can replace this with reading resources directory
public enum FormType {
    LCST12("Licking County Stream Team 2012", null),
    SWQ20("Ohio Stream Water Quality 2020", null);

    private final String name;
    private final String file;

    FormType(String name, String resourceFile) {
        this.name = name;
        this.file = resourceFile;
    }

    public String getName() {
        return name;
    }

    public String getFile() {
        return file;
    }
}
