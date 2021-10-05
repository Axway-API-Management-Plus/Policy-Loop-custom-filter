package rslr.circuit.loop;

import com.vordel.circuit.Filter;
import com.vordel.client.circuit.model.Circuit;
import com.vordel.client.circuit.model.CircuitContext;
import com.vordel.client.circuit.model.CircuitStore;
import com.vordel.client.circuit.model.ICircuitDelegate;
import com.vordel.client.manager.Manager;
import com.vordel.client.manager.ManagerEntityStore;
import com.vordel.client.manager.filter.DefaultGUIFilter;
import com.vordel.client.manager.filter.DelegatingFilterSynchronizer;
import com.vordel.common.util.PropDef;
import com.vordel.config.ConfigContext;
import com.vordel.es.ESPK;
import com.vordel.es.Entity;
import com.vordel.es.EntityStore;
import com.vordel.es.EntityStoreException;
import com.vordel.trace.Trace;

import java.util.Set;

public class CircuitLoopGUIFilter extends DefaultGUIFilter implements ICircuitDelegate {
	public static final String ERR_PARENTPOINTER = "Configuration error the circuit loop '%s' is pointing to it's parent circuit";

	@Override
	public void filterAttached(ConfigContext ctx, Entity entity) throws EntityStoreException {
		super.filterAttached(ctx, entity);

		ManagerEntityStore mstore = (ManagerEntityStore) ctx.getStore();
		CircuitLoopFilter filter = (CircuitLoopFilter) getFilter();

		entityUpdated(filter);

		new DelegatingFilterSynchronizer(mstore, filter, this);
	}

	@Override
	public void entityUpdated(Entity entity) {
		super.entityUpdated(entity);

		CircuitLoopFilter filter = (CircuitLoopFilter) getFilter();
		Circuit loopCircuit = getLoopCircuit();

		ESPK nextCircuitPK = filter.getLoopCircuitPK();
		ESPK previousCircuitPK = loopCircuit == null ? EntityStore.ES_NULL_PK : loopCircuit.getEntity().getPK();

		if (!previousCircuitPK.equals(nextCircuitPK)) {
			entityUpdated(filter);
		}
	}

	private void entityUpdated(CircuitLoopFilter filter) {
		ESPK circuitPK = filter.getLoopCircuitPK();

		setLoopCircuit(null);

		if ((circuitPK != null) && (!EntityStore.ES_NULL_PK.equals(circuitPK))) {
			if (getEntity().getParentPK().equals(circuitPK)) {
				
				Trace.fatal(String.format(ERR_PARENTPOINTER, getFilter().getName()));
			} else {
				CircuitStore store = Manager.getInstance().getSelectedEntityStore().getCircuitStore();
				Circuit loopCircuit = store.getCircuit(circuitPK);

				if (loopCircuit != null) {
					setLoopCircuit(loopCircuit);
				}
			}
		}
	}

	private Circuit loopCircuit = null;

	private Circuit getLoopCircuit() {
		return loopCircuit;
	}

	private void setLoopCircuit(Circuit loopCircuit) {
		this.loopCircuit = loopCircuit;
	}

	@Override
	public Set<PropDef> getAvailableProperties(ESPK toDelegate) {
		return super.getAvailableProperties();
	}

	@Override
	public Set<PropDef> getAvailableProperties(ESPK from, ESPK toDelegate) {
		return getAvailableProperties(toDelegate);
	}

	@Override
	public Set<PropDef> getGeneratedProperties(ESPK toDelegate) {
		return Filter.EMPTY_PROP_DEF_SET;
	}

	@Override
	public int compareTo(CircuitContext other) {
		return toString().compareTo(other.toString());
	}

	@Override
	public boolean delegatesTo(Circuit c) {
		CircuitLoopFilter filter = (CircuitLoopFilter) getFilter();
		ESPK loopCircuitPK = filter.getLoopCircuitPK();
		ESPK cpk = c.getEntity().getPK();

		return loopCircuitPK.equals(cpk);
	}

	@Override
	public String toString() {
		return (circuit == null ? "" : circuit.getName() + ":") + getName();
	}
}
