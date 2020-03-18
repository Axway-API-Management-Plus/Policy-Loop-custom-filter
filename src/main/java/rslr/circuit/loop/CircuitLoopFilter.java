package rslr.circuit.loop;

import java.util.HashSet;
import java.util.Set;

import com.vordel.circuit.DefaultFilter;
import com.vordel.circuit.DelegatingFilter;
import com.vordel.circuit.FilterContainerImpl;
import com.vordel.circuit.GlobalProperties;
import com.vordel.circuit.MessageProcessor;
import com.vordel.config.ConfigContext;
import com.vordel.es.ESPK;
import com.vordel.es.Entity;
import com.vordel.es.EntityStore;
import com.vordel.es.EntityStoreException;

public class CircuitLoopFilter extends DefaultFilter implements DelegatingFilter {
	@Override
	public Class<? extends MessageProcessor> getMessageProcessorClass() throws ClassNotFoundException {
		return CircuitLoopProcessor.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends FilterContainerImpl> getConfigPanelClass() throws ClassNotFoundException {
		return (Class<? extends FilterContainerImpl>) Class.forName("rslr.circuit.loop.CircuitLoopGUIFilter");
	}

	private ESPK loopCircuitPK = EntityStore.ES_NULL_PK;

	public ESPK getLoopCircuitPK() {
		return loopCircuitPK;
	}

	public void setLoopCircuitPK(ESPK loopCircuitPK) {
		this.loopCircuitPK = loopCircuitPK;
	}

	public void setEntity(Entity e) {
		super.setEntity(e);

		setLoopCircuitPK(e.getReferenceValue("loopCircuit"));
	}

	@Override
	public ESPK getPK() {
		return getEntity().getPK();
	}

	@Override
	public Set<ESPK> getReferencedCircuitPKs(GlobalProperties props) {
		ESPK loopCircuitPK = getLoopCircuitPK();
		Set<ESPK> pks = new HashSet<>();

		if (!EntityStore.ES_NULL_PK.equals(loopCircuitPK)) {
			pks.add(loopCircuitPK);
		}

		return pks;
	}

	public void updateRefs(ConfigContext ctx) {
		try {
			configure(ctx, getEntity());
		} catch (EntityStoreException ese) {
			throw new Error("Couldn't update references in circuit chain", ese);
		}
	}

	public void updateRefs() {
	}
}
