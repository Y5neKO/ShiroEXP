package com.y5neko.shiroexp.copyright;

import com.y5neko.shiroexp.misc.Tools;

public class Copyright {
    public static final String version = "2.0";

     public static String logo = "\n" +
            "   _____    __      _                    ______   _  __    ____ \n" +
            "  / ___/   / /_    (_)   _____  ____    / ____/  | |/ /   / __ \\\n" +
            "  \\__ \\   / __ \\  / /   / ___/ / __ \\  / __/     |   /   / /_/ /\n" +
            " ___/ /  / / / / / /   / /    / /_/ / / /___    /   |   / ____/ \n" +
            "/____/  /_/ /_/ /_/   /_/     \\____/ /_____/   /_/|_|  /_/      \n" +
            "                                                       v%s by %s :)\n" +
            "                                                       GitHub: https://github.com/Y5neKO\n";

     public static String getLogo() {
         return String.format(logo, Tools.color(version, "CYAN"), Tools.color("Y5neKO", "YELLOW"));
     }
}
