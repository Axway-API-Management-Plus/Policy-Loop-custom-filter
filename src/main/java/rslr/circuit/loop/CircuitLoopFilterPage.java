package rslr.circuit.loop;

import org.eclipse.swt.widgets.Composite;

import com.vordel.client.manager.wizard.VordelPage;

public class CircuitLoopFilterPage extends VordelPage {
	public CircuitLoopFilterPage() {
		super("circuitloopPage");

		setTitle(Resources.CIRCUITLOOP_PAGE);
		setDescription(Resources.CIRCUITLOOP_PAGE_DESCRIPTION);
		setPageComplete(false);
	}

	public String getHelpID() {
		return "circuitloop.help";
	}

	public boolean performFinish() {
		return true;
	}

	public void createControl(Composite parent) {
		Composite panel = render(parent, getClass().getResourceAsStream("page.xml"));

		setControl(panel);
		setPageComplete(true);
	}
}
