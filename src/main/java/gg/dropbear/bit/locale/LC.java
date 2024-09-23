package gg.dropbear.bit.locale;

import gg.dropbear.bit.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum LC {
    info_Console("info_Same", 0, "&cCan't open your own inventory for editing!");

    private String text;
    private List<String> comments;
    private LC(final String s, final int n, final String s2) {
        this(s, n, s2, new String[] { "" });
    }

    private LC(final String name, final int ordinal, final String text, final String... array) {
        this.comments = new ArrayList<String>();
        this.text = text;
        if (array != null && array.length > 0) {
            for (final String s : array) {
                if (!s.isEmpty()) {
                    this.comments.add(s);
                }
            }
        }
    }

    private LC(final List<String> list) {
        this(list, new String[] { "" });
    }

    private LC(final List<String> list, final String[] array) {
        this.comments = new ArrayList<String>();
        if (this.text == null) {
            this.text = "";
        }
        for (final String str : list) {
            if (!this.text.isEmpty()) {
                this.text = String.valueOf(this.text) + " /n";
            }
            this.text = String.valueOf(this.text) + str;
        }
        if (array != null && array.length > 0) {
            for (final String s : array) {
                if (!s.isEmpty()) {
                    this.comments.add(s);
                }
            }
        }
    }

    public String getLocale(final Object... array) {
        return getMsg(this, array);
    }

    private static String getMsg(LC DBlc, Object... copyOfRange) {
        LC DBlc2 = null;
        if (copyOfRange.length > 0 && copyOfRange[0] instanceof LC) {
            DBlc2 = DBlc;
            DBlc = (LC)copyOfRange[0];
            copyOfRange = Arrays.copyOfRange(copyOfRange, 1, copyOfRange.length);
        }
        if (DBlc == null) {
            return "";
        }
        if (DBlc2 == null) {
            if (!Main.getInstance().getLM().isList(DBlc.getPt())) {
                return Main.getInstance().getLM().getMessage(DBlc.getPt(), copyOfRange);
            }
            final List<String> messageList = Main.getInstance().getLM().getMessageList(DBlc.getPt(), copyOfRange);
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < messageList.size(); ++i) {
                sb.append(messageList.get(i));
                if (i < messageList.size()) {
                    sb.append(" /n ");
                }
            }
            return sb.toString();
        }
        else {
            if (!Main.getInstance().getLM().isList(DBlc2.getPt())) {
                return String.valueOf(Main.getInstance().getLM().getMessage(DBlc2.getPt(), new Object[0])) + Main.getInstance().getLM().getMessage(DBlc.getPt(), copyOfRange);
            }
            final List<String> messageList2 = Main.getInstance().getLM().getMessageList(DBlc2.getPt(), copyOfRange);
            final StringBuilder sb2 = new StringBuilder();
            for (int j = 0; j < messageList2.size(); ++j) {
                sb2.append(messageList2.get(j));
                if (j < messageList2.size()) {
                    sb2.append(" /n ");
                }
            }
            return Main.getInstance().getPlaceholderAPIManager().updatePlaceHolders(sb2.toString());
        }
    }

    public String getPt() {
        return this.name().replace("_", ".");
    }
}
