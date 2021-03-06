package org.jpacman.framework.model;

import java.util.ArrayList;

import org.jpacman.framework.ui.UndoButtonPanel;
import org.jpacman.framework.ui.UndoablePacman;

/**
 * Special version of the Game which implements and undo function.
 * 
 * @author Rick van Hattem <Rick.van.Hattem@Fawo.nl>
 */
public class UndoableGame extends Game {
	/*
	 * Since I'd like to have both undo and redo, a deque is not too convenient. The normal iterator
	 * only goes 1 way
	 */
	private ArrayList<GameState> states;
	private int stateIndex;
	private final UndoablePacman undoablePacman;

	/**
	 * Initialize the states history for the games.
	 * 
	 * @param undoablePacman
	 *            The undoable pacman to implement undo/redo.
	 */
	public UndoableGame(UndoablePacman undoablePacman) {
		this.states = new ArrayList<GameState>();
		this.undoablePacman = undoablePacman;
		this.stateIndex = 0;
	}

	/**
	 * @return Wheter we can redo an undo.
	 */
	public boolean canRedo() {
		return this.stateIndex < this.states.size() - 1;
	}

	/**
	 * @return Whether there is a state to undo.
	 */
	public boolean canUndo() {
		return this.stateIndex > 0;
	}

	@Override
	public UndoableBoard getBoard() {
		return (UndoableBoard) super.getBoard();
	}

	private void loadState(GameState state) {
		if (this.undoablePacman.eventHandler() != null) {
			this.undoablePacman.eventHandler().stop();
		}
		state.restoreTo(this);
		toggleButtons();
		notifyViewers();
	}

	@Override
	public void movePlayer(Direction dir) {
		Player player = getPlayer();
		Tile currentTile = player.getTile();

		GameState gameState = new GameState(getBoard());
		super.movePlayer(dir);

		/* If we moved the player, we want to add an undo state */
		if (currentTile != player.getTile()) {
			/*
			 * Remove all states after this state, redoing should only be possible if we haven't
			 * moved since the last undo
			 */
			this.states = new ArrayList<GameState>(this.states.subList(0,
					this.stateIndex));

			/* Save the state to restore later */
			this.stateIndex = saveState(gameState);
		}

		toggleButtons();
	}

	/**
	 * Redo the last undoed move.
	 */
	public void redo() {
		if (canRedo()) {
			loadState(this.states.get(++this.stateIndex));
		}
	}

	private int saveState(GameState gameState) {
		this.states.add(gameState);
		return this.states.size();
	}

	private void toggleButtons() {
		UndoButtonPanel buttonPanel = this.undoablePacman.getButtonPanel();
		if (buttonPanel != null) {
			buttonPanel.toggleUndo(canUndo());
			buttonPanel.toggleRedo(canRedo());
		}
	}

	/**
	 * Undo the last move.
	 */
	public void undo() {
		if (canUndo()) {
			if (this.stateIndex == this.states.size()) {
				saveState(new GameState(getBoard()));
			}
			loadState(this.states.get(--this.stateIndex));
		}
	}
}
