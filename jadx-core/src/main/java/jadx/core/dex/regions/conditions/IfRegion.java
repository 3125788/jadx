package jadx.core.dex.regions.conditions;

import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;
import jadx.core.dex.regions.AbstractRegion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class IfRegion extends AbstractRegion {

	private final BlockNode header;

	private IfCondition condition;
	private IContainer thenRegion;
	private IContainer elseRegion;

	public IfRegion(IRegion parent, BlockNode header) {
		super(parent);
		assert header.getInstructions().size() == 1;
		this.header = header;
		this.condition = IfCondition.fromIfBlock(header);
	}

	public IfCondition getCondition() {
		return condition;
	}

	public void setCondition(IfCondition condition) {
		this.condition = condition;
	}

	public IContainer getThenRegion() {
		return thenRegion;
	}

	public void setThenRegion(IContainer thenRegion) {
		this.thenRegion = thenRegion;
	}

	public IContainer getElseRegion() {
		return elseRegion;
	}

	public void setElseRegion(IContainer elseRegion) {
		this.elseRegion = elseRegion;
	}

	public BlockNode getHeader() {
		return header;
	}

	public boolean simplifyCondition() {
		IfCondition cond = IfCondition.simplify(condition);
		if (cond != condition) {
			condition = cond;
			return true;
		}
		return false;
	}

	public void invert() {
		condition = IfCondition.invert(condition);
		// swap regions
		IContainer tmp = thenRegion;
		thenRegion = elseRegion;
		elseRegion = tmp;
	}

	public int getSourceLine() {
		if (header.getInstructions().isEmpty()) {
			return 0;
		}
		return header.getInstructions().get(0).getSourceLine();
	}

	@Override
	public List<IContainer> getSubBlocks() {
		List<IContainer> all = new ArrayList<IContainer>(3);
		all.add(header);
		if (thenRegion != null) {
			all.add(thenRegion);
		}
		if (elseRegion != null) {
			all.add(elseRegion);
		}
		return Collections.unmodifiableList(all);
	}

	@Override
	public boolean replaceSubBlock(IContainer oldBlock, IContainer newBlock) {
		if (oldBlock == thenRegion) {
			thenRegion = newBlock;
			return true;
		}
		if (oldBlock == elseRegion) {
			elseRegion = newBlock;
			return true;
		}
		return false;
	}

	@Override
	public String baseString() {
		StringBuilder sb = new StringBuilder();
		if (thenRegion != null) {
			sb.append(thenRegion.baseString());
		}
		if (elseRegion != null) {
			sb.append(elseRegion.baseString());
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return "IF(" + condition + ") then " + thenRegion + " else " + elseRegion;
	}
}
