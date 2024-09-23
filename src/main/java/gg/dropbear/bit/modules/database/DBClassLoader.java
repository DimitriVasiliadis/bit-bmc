package gg.dropbear.bit.modules.database;

import gg.dropbear.bit.Main;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class DBClassLoader extends URLClassLoader {
    public DBClassLoader(final Main DB) {
        super(new URL[0], DB.getClass().getClassLoader());
    }

    public void addFile(final File file) throws MalformedURLException {
        this.addURL(file.toURI().toURL());
    }

    public void addURL(final URL url) {
        URL[] urLs;
        for (int length = (urLs = this.getURLs()).length, i = 0; i < length; ++i) {
            if (url.sameFile(urLs[i])) {
                return;
            }
        }
        super.addURL(url);
    }
}
