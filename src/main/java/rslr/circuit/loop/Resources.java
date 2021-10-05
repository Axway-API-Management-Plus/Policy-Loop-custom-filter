package rslr.circuit.loop;

import org.eclipse.osgi.util.NLS;

public class Resources extends NLS {

    private static final String BUNDLE_NAME = "rslr.circuit.loop.resources"; //$NON-NLS-1$

    public static String CIRCUITLOOP_PAGE;
    public static String CIRCUITLOOP_PAGE_DESCRIPTION;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Resources.class);
    }

    private Resources() {
        super();
    }
}
