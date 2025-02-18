package jadx.core.dex.nodes;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.AttrNode;
import jadx.core.dex.attributes.nodes.LoopInfo;
import jadx.core.utils.InsnUtils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BlockNode extends AttrNode implements IBlock {

	private int id;
	private final int startOffset;
	private final List<InsnNode> instructions = new ArrayList<InsnNode>(2);

	private List<BlockNode> predecessors = new ArrayList<BlockNode>(1);
	private List<BlockNode> successors = new ArrayList<BlockNode>(1);
	private List<BlockNode> cleanSuccessors;

	// all dominators
	private BitSet doms;
	// dominance frontier
	private BitSet domFrontier;
	// immediate dominator
	private BlockNode idom;
	// blocks on which dominates this block
	private List<BlockNode> dominatesOn = Collections.emptyList();

	public BlockNode(int id, int offset) {
		this.id = id;
		this.startOffset = offset;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public List<BlockNode> getPredecessors() {
		return predecessors;
	}

	public List<BlockNode> getSuccessors() {
		return successors;
	}

	public List<BlockNode> getCleanSuccessors() {
		return cleanSuccessors;
	}

	public void updateCleanSuccessors() {
		cleanSuccessors = cleanSuccessors(this);
	}

	public void lock() {
		cleanSuccessors = lockList(cleanSuccessors);
		successors = lockList(successors);
		predecessors = lockList(predecessors);
		dominatesOn = lockList(dominatesOn);
	}

	List<BlockNode> lockList(List<BlockNode> list) {
		if (list.isEmpty()) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(list);
	}

	/**
	 * Return all successor which are not exception handler or followed by loop back edge
	 */
	private static List<BlockNode> cleanSuccessors(BlockNode block) {
		List<BlockNode> sucList = block.getSuccessors();
		if (sucList.isEmpty()) {
			return sucList;
		}
		List<BlockNode> toRemove = new LinkedList<BlockNode>();
		for (BlockNode b : sucList) {
			if (b.contains(AType.EXC_HANDLER)) {
				toRemove.add(b);
			} else if (b.contains(AFlag.SYNTHETIC)) {
				List<BlockNode> s = b.getSuccessors();
				if (s.size() == 1 && s.get(0).contains(AType.EXC_HANDLER)) {
					toRemove.add(b);
				}
			}
		}
		if (block.contains(AFlag.LOOP_END)) {
			List<LoopInfo> loops = block.getAll(AType.LOOP);
			for (LoopInfo loop : loops) {
				toRemove.add(loop.getStart());
			}
		}
		if (toRemove.isEmpty()) {
			return sucList;
		}
		List<BlockNode> result = new ArrayList<BlockNode>(sucList);
		result.removeAll(toRemove);
		return result;
	}

	@Override
	public List<InsnNode> getInstructions() {
		return instructions;
	}

	public int getStartOffset() {
		return startOffset;
	}

	/**
	 * Check if 'block' dominated on this node
	 */
	public boolean isDominator(BlockNode block) {
		return doms.get(block.getId());
	}

	/**
	 * Dominators of this node (exclude itself)
	 */
	public BitSet getDoms() {
		return doms;
	}

	public void setDoms(BitSet doms) {
		this.doms = doms;
	}

	public BitSet getDomFrontier() {
		return domFrontier;
	}

	public void setDomFrontier(BitSet domFrontier) {
		this.domFrontier = domFrontier;
	}

	/**
	 * Immediate dominator
	 */
	public BlockNode getIDom() {
		return idom;
	}

	public void setIDom(BlockNode idom) {
		this.idom = idom;
	}

	public List<BlockNode> getDominatesOn() {
		return dominatesOn;
	}

	public void addDominatesOn(BlockNode block) {
		if (dominatesOn.isEmpty()) {
			dominatesOn = new LinkedList<BlockNode>();
		}
		dominatesOn.add(block);
	}

	public boolean isSynthetic() {
		return contains(AFlag.SYNTHETIC);
	}

	public boolean isReturnBlock() {
		return contains(AFlag.RETURN);
	}

	@Override
	public int hashCode() {
		return id; // TODO id can change during reindex
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (hashCode() != obj.hashCode()) {
			return false;
		}
		if (!(obj instanceof BlockNode)) {
			return false;
		}
		BlockNode other = (BlockNode) obj;
		if (id != other.id) {
			return false;
		}
		if (startOffset != other.startOffset) {
			return false;
		}
		return true;
	}

	@Override
	public String baseString() {
		return Integer.toString(id);
	}

	@Override
	public String toString() {
		return "B:" + id + ":" + InsnUtils.formatOffset(startOffset);
	}
}
