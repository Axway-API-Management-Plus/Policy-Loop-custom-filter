package rslr.circuit.loop;

import com.vordel.circuit.CircuitAbortException;
import com.vordel.circuit.InvocationEngine;
import com.vordel.circuit.Message;
import com.vordel.circuit.MessageProcessor;
import com.vordel.common.Dictionary;
import com.vordel.config.Circuit;
import com.vordel.config.ConfigContext;
import com.vordel.dwe.DelayedESPK;
import com.vordel.el.Selector;
import com.vordel.es.ESPK;
import com.vordel.es.Entity;
import com.vordel.es.EntityStore;
import com.vordel.es.EntityStoreException;

public class CircuitLoopProcessor extends MessageProcessor {
	public static final int LOOPTYPE_WHILE = 1;
	public static final int LOOPTYPE_DOWHILE = 2;

	private Selector<Integer> loopType;
	private Selector<Boolean> loopCondition;
	private Selector<Integer> loopMax;
	private Selector<Integer> loopTimeout;

	private Selector<Boolean> loopErrorCircuit;
	private Selector<Boolean> loopErrorCondition;
	private Selector<Boolean> loopErrorMax;
	private Selector<Boolean> loopErrorTimeout;
	private Selector<Boolean> loopErrorEmpty;

	private Circuit loopCircuit;
	private ESPK loopContext;

	@Override
	public void filterAttached(ConfigContext ctx, Entity entity) throws EntityStoreException {
		super.filterAttached(ctx, entity);

		this.loopType = new Selector<Integer>(entity.getStringValue("loopType"), Integer.class);
		this.loopCondition = new Selector<Boolean>(entity.getStringValue("loopCondition"), Boolean.class);
		this.loopMax = new Selector<Integer>(entity.getStringValue("loopMax"), Integer.class);
		this.loopTimeout = new Selector<Integer>(entity.getStringValue("loopTimeout"), Integer.class);

		this.loopErrorCircuit = new Selector<Boolean>(entity.getStringValue("loopErrorCircuit"), Boolean.class);
		this.loopErrorCondition = new Selector<Boolean>(entity.getStringValue("loopErrorCondition"), Boolean.class);
		this.loopErrorMax = new Selector<Boolean>(entity.getStringValue("loopErrorMax"), Boolean.class);
		this.loopErrorTimeout = new Selector<Boolean>(entity.getStringValue("loopErrorTimeout"), Boolean.class);
		this.loopErrorEmpty = new Selector<Boolean>(entity.getStringValue("loopErrorEmpty"), Boolean.class);

		CircuitLoopFilter filter = (CircuitLoopFilter) getFilter();
		DelayedESPK loopReference = new DelayedESPK(filter.getLoopCircuitPK());
		ESPK loopPK = loopReference.substitute(Dictionary.empty);
		Circuit loopCircuit = null;

		/*
		 * Ensure we have a configured circuit and we do not loop on our parent
		 * policy
		 */
		if ((!EntityStore.ES_NULL_PK.equals(loopPK) && (!entity.getParentPK().equals(loopPK)))) {
			loopCircuit = ctx.getCircuit(loopPK);

			this.loopContext = loopPK;
		}

		this.loopCircuit = loopCircuit;
	}

	@Override
	public boolean invoke(Circuit p, Message m) throws CircuitAbortException {
		Integer type = loopType.substitute(m);

		long start = System.currentTimeMillis();
		long timeout = timeout(m);

		if (type == null) {
			throw new CircuitAbortException("Unable to compute loop type " + loopType.getLiteral());
		}

		boolean result = true;
		boolean loop = false;
		int max = max(m);
		int count = 0;

		/*
		 * difference between while and do/while loops is only for the first
		 * round
		 */
		switch (type) {
		case LOOPTYPE_WHILE:
			/* check condition before first round */
			loop = condition(m);

			if (!loop) {
				/* we are going to skip the loop, check for error condition */
				result = !isErrorCondition(m, loopErrorEmpty);
			}
			break;
		case LOOPTYPE_DOWHILE:
			/* check condition after first round */
			loop = true;
			break;
		default:
			throw new CircuitAbortException("Wrong loop type '" + type + "' (only 1 and 2 permitted)");
		}

		while (loop) {
			/* execute loop (and exit if the loop circuit return false) */
			loop = executeLoop(p, m);

			if (!loop) {
				/*
				 * loop circuit did return an error, check for error condition
				 */
				result = !isErrorCondition(m, loopErrorCircuit);
			} else if (timeout > 0) {
				/* Check if the current loop has expired */
				loop = System.currentTimeMillis() <= (start + timeout);

				if (!loop) {
					/*
					 * expiration time has exhausted, check for error condition
					 */
					result = !isErrorCondition(m, loopErrorTimeout);
				} else if (max > 0) {
					count++;

					/* Check if the maximum iteration count has been reached */
					loop = count < max;

					if (!loop) {
						/*
						 * max iteration count was exhausted, check for error
						 * condition
						 */
						result = !isErrorCondition(m, loopErrorMax);
					} else {
						/* check if we need more iteration */
						loop = condition(m);

						if (!loop) {
							/* not looping anymore, check for error condition */
							result = !isErrorCondition(m, loopErrorCondition);
						}
					}
				}
			}
		}

		return result;
	}

	private boolean isErrorCondition(Message m, Selector<Boolean> attribute) throws CircuitAbortException {
		Boolean result = attribute.substitute(m);

		if (result == null) {
			throw new CircuitAbortException("Could not evaluate boolean expression " + attribute.getLiteral());
		}

		return result;
	}

	private boolean condition(Message m) throws CircuitAbortException {
		Boolean result = loopCondition.substitute(m);

		if (result == null) {
			throw new CircuitAbortException("Could not evaluate boolean expression " + loopCondition.getLiteral());
		}

		return result;
	}

	private int max(Message m) throws CircuitAbortException {
		Integer result = loopMax.substitute(m);

		if (result == null) {
			throw new CircuitAbortException("Could not evaluate maximum loop iteration count " + loopMax.getLiteral());
		}

		if (result < 0) {
			throw new CircuitAbortException("Can't have negative maximum iteration count");
		}

		return result;
	}

	private int timeout(Message m) throws CircuitAbortException {
		Integer result = loopTimeout.substitute(m);

		if (result == null) {
			throw new CircuitAbortException("Could not evaluate loop timeout " + loopMax.getLiteral());
		}

		if (result < 0) {
			throw new CircuitAbortException("Can't have negative maximum loop timeout");
		}

		return result;
	}

	private boolean executeLoop(Circuit p, Message m) throws CircuitAbortException {
		return loopCircuit == null ? false : InvocationEngine.invokeCircuit(loopCircuit, loopContext, m);
	}
}
