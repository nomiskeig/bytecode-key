package org.key_project.key4eclipse.test.testcase.swtbot;

import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;

import org.junit.Test;
import org.key_project.key4eclipse.starter.core.util.KeYUtil;
import org.key_project.key4eclipse.util.test.util.TestUtilsUtil;
import org.key_project.swtbot.swing.bot.SwingBotJFrame;

import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.gui.MainWindow;

/**
 * Tests for {@link Main}.
 * @author Martin Hentschel
 */
public class SWTBotMainTest extends TestCase {
    /**
     * Starts the normal KeY application via {@link KeYUtil#openMainWindowAsync()}
     * and closes it. If something is wrong with the KeY eclipse integration
     * an exception is thrown. 
     */
    @Test
    public void testOpeningMainWindow() throws InterruptedException, InvocationTargetException {
        // Open KeY user interface and make sure that a window is opened.
        KeYUtil.openMainWindowAsync();
        SwingBotJFrame frame = TestUtilsUtil.keyGetMainWindow();
        assertTrue(frame.isOpen());
        assertNotNull(MainWindow.getInstance());
        assertTrue(MainWindow.getInstance().isVisible());
        // Get main window and close it
        MainWindow.getInstance().setVisible(false);
        assertFalse(MainWindow.getInstance().isVisible());
        assertFalse(frame.isOpen());
    }
}