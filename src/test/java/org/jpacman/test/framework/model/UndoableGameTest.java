/**
 * 
 */
package org.jpacman.test.framework.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import org.hamcrest.CoreMatchers;
import org.jpacman.framework.factory.FactoryException;
import org.jpacman.framework.factory.IGameFactory;
import org.jpacman.framework.factory.MapParser;
import org.jpacman.framework.factory.UndoableGameFactory;
import org.jpacman.framework.model.Direction;
import org.jpacman.framework.model.UndoableGame;
import org.jpacman.framework.model.IBoardInspector.SpriteType;
import org.jpacman.framework.ui.UndoablePacman;
import org.junit.Test;
import org.mockito.Mockito;
import org.jpacman.framework.model.Food;
import org.jpacman.framework.model.Ghost;
import org.jpacman.framework.model.IBoardInspector.SpriteType;
import org.jpacman.framework.model.Player;
import org.jpacman.framework.model.Tile;
import org.jpacman.framework.model.UndoableGame;
import org.jpacman.framework.ui.UndoablePacman;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Rick van Hattem <Rick.van.Hattem@Fawo.nl>
 * @author Yanick van Langeraad
 */
public class UndoableGameTest extends GameTest {

	@Override
	public IGameFactory makeFactory() {
		return new UndoableGameFactory(new UndoablePacman());
	}

	@Override
	protected UndoableGame makePlay(String singleRow) throws FactoryException {
		return (UndoableGame) super.makePlay(singleRow);
	}
	
	/**
	 * Test the initial state, when the game is just made
	 * @throws FactoryException
	 */
	@Test
	public void testInitial() throws FactoryException{
		UndoableGame g = makePlay("P");
		assertEquals(g.getPlayer(), g.getBoard().spriteAt(0, 0));
		assertThat(tileAt(g, 0, 0), equalTo(g.getPlayer().getTile()));
		assertEquals(SpriteType.PLAYER, g.getBoard().spriteTypeAt(0, 0));
		assertEquals(0, g.getPlayer().getPoints());
		assertTrue(g.getPlayer().isAlive());
	}
	
	/**
	 * Tests if the Game can Undo under the right circumstances
	 * @throws FactoryException
	 */
	@Test
	public void canUndoTest() throws FactoryException{
		UndoableGame g = makePlay("P");
		assertFalse(g.canUndo());
	}
	
	/**
	 * This one tests canUndo again, and also the canRedo function
	 * @throws FactoryException
	 */
	@Test
	public void canRedoTest() throws FactoryException{
		UndoableGame g = makePlay("P #");
		g.movePlayer(Direction.RIGHT);
		assertTrue(g.canUndo());
		assertFalse(g.canRedo());
		g.undo();
		assertFalse(g.canUndo());
		assertTrue(g.canRedo());
	}
	
	/**
	 * This tests whether really nothing has changed when you try to Undo before moving.
	 * @throws FactoryException
	 */
	@Test
	public void UndoNothing() throws FactoryException{
		UndoableGame g = makePlay("P");
		assertEquals(g.getPlayer(), g.getBoard().spriteAt(0, 0));
		g.undo();
		assertEquals(g.getPlayer(), g.getBoard().spriteAt(0, 0));
	}
	
	/**
	 * This tests whether you can Undo after only walking against a wall (it shouldn't be able to)
	 * @throws FactoryException
	 */
	@Test
	public void UndoWall_1() throws FactoryException{
		UndoableGame g = makePlay("P#");
		assertEquals(g.getPlayer(), g.getBoard().spriteAt(0, 0));
		g.movePlayer(Direction.RIGHT);
		assertEquals(g.getPlayer(), g.getBoard().spriteAt(0, 0));
		assertFalse(g.canUndo());
	}
	
	/**
	 * This tests whether Undo really ignores walking against a wall
	 * @throws FactoryException
	 */
	@Test
	public void UndoWall_2() throws FactoryException{
		UndoableGame g = makePlay("P #");
		assertEquals(g.getPlayer(), g.getBoard().spriteAt(0, 0));
		g.movePlayer(Direction.RIGHT);
		assertEquals(g.getPlayer(), g.getBoard().spriteAt(1, 0));
		g.movePlayer(Direction.RIGHT);
		assertEquals(g.getPlayer(), g.getBoard().spriteAt(1, 0));
		g.undo();
		assertEquals(g.getPlayer(), g.getBoard().spriteAt(0, 0));
	}
	
	/**
	 * This tests whether a player will die when walking against a Ghost
	 * And if, after undoing, the player will once again live.
	 * @throws FactoryException
	 */
	@Test
	public void Undo_Player_to_Ghost() throws FactoryException{
		UndoableGame g = makePlay("PG");
		assertEquals(g.getPlayer(), g.getBoard().spriteAt(0, 0));
		g.movePlayer(Direction.RIGHT);
		assertEquals(g.getPlayer(), g.getBoard().spriteAt(1, 0));
		assertFalse(g.getPlayer().isAlive());
		g.undo();
		assertEquals(g.getPlayer(), g.getBoard().spriteAt(0, 0));
		assertTrue(g.getPlayer().isAlive());
	}
	
	/**
	 * Now one big test to show the all functionality, and that it works in succession.
	 * @throws FactoryException
	 */
	@Test
	public void UndoLotsAndRedoLots() throws FactoryException{
		UndoableGame g = makePlay("#. P ... ..G#");
		//Starting at (3,0) with 0 points
		assertEquals(g.getPlayer(), g.getBoard().spriteAt(3, 0));
		assertEquals(0, g.getPlayer().getPoints());
		
		//After moving right twice we should have 10 points at (5,0)
		g.movePlayer(Direction.RIGHT);
		g.movePlayer(Direction.RIGHT);
		assertEquals(g.getPlayer(), g.getBoard().spriteAt(5, 0));
		assertEquals(10, g.getPlayer().getPoints());
		//Undo the last move, we should have no points at (4,0)
		g.undo();
		assertEquals(g.getPlayer(), g.getBoard().spriteAt(4, 0));
		assertEquals(0, g.getPlayer().getPoints());
		//Redo this, we should have 10 points at (5,0)
		g.redo();
		assertEquals(g.getPlayer(), g.getBoard().spriteAt(5, 0));
		assertEquals(10, g.getPlayer().getPoints());
		
		//Move right 6 times, should now be on the spot of the Ghost ((11,0))
		for(int i = 0; i<6; i++)
			g.movePlayer(Direction.RIGHT);

		
		assertEquals(g.getPlayer(), g.getBoard().spriteAt(11, 0));
		//And thus be dead
		assertFalse(g.getPlayer().isAlive());
		//But we should have 50 points
		assertEquals(50, g.getPlayer().getPoints());
		
		//Undo trice
		g.undo();
		g.undo();
		g.undo();
		//We should now be at (8,0) with 30 points, alive and kicking
		assertEquals(g.getPlayer(), g.getBoard().spriteAt(8, 0));
		assertEquals(30, g.getPlayer().getPoints());
		assertTrue(g.getPlayer().isAlive());
		
		//redo twice, we should be at (10,0) with 50 points
		g.redo();
		g.redo();
		assertEquals(g.getPlayer(), g.getBoard().spriteAt(10, 0));
		assertEquals(50, g.getPlayer().getPoints());
		
		//Move left 9 times for the last bit of food
		for(int i = 0; i<9; i++)
			g.movePlayer(Direction.LEFT);
		
		assertEquals(g.getPlayer(), g.getBoard().spriteAt(1, 0));
		assertEquals(60, g.getPlayer().getPoints());
	}
	


	/**
	 * Test what happens if the ghost moves into the player and that an undo reverts it all.
	 * 
	 * @throws FactoryException
	 *             Never.
	 */
	@Test
	public void testC4a_GhostMovesToPlayer() throws FactoryException {
		UndoableGame game = makePlay("PG#");
		Ghost ghost = (Ghost) game.getBoard().spriteAt(1, 0);
		Player player = game.getPlayer();

		game.moveGhost(ghost, Direction.LEFT);
		Assert.assertFalse("Move kills player", player.isAlive());

		Tile newTile = ghost.getTile();
		Assert.assertThat(tileAt(game, 0, 0), CoreMatchers.equalTo(newTile));

		/* Ghosts don't create new states so undo changes nothing */
		game.undo();
		Assert.assertFalse("Undo changes nothing", player.isAlive());
		Assert.assertThat(tileAt(game, 0, 0),
				CoreMatchers.equalTo(ghost.getTile()));
	}

	/**
	 * Test what happens if the ghost moves into the player and that an undo reverts it all.
	 * 
	 * @throws FactoryException
	 *             Never.
	 */
	@Test
	public void testC4b_GhostMovesToPlayer() throws FactoryException {
		UndoableGame game = makePlay("P.G");
		Ghost ghost = (Ghost) game.getBoard().spriteAt(2, 0);
		Player player = game.getPlayer();

		game.movePlayer(Direction.RIGHT);
		game.moveGhost(ghost, Direction.LEFT);
		Assert.assertFalse("Move kills player", player.isAlive());

		Tile newTile = ghost.getTile();
		Assert.assertThat(tileAt(game, 1, 0), CoreMatchers.equalTo(newTile));

		/* Ghosts don't create new states so undo changes nothing */
		game.undo();
		Assert.assertTrue("Player revives", player.isAlive());
		Assert.assertThat(tileAt(game, 2, 0),
				CoreMatchers.equalTo(ghost.getTile()));
		Assert.assertThat(tileAt(game, 0, 0),
				CoreMatchers.equalTo(player.getTile()));
	}

	/**
	 * Test that a player indeed consumes food if he enters food and that an undo reverts it all.
	 * 
	 * @throws FactoryException
	 *             Never.
	 */
	@Override
	@Test
	public void testC5_PlayerMovesToFood() throws FactoryException {
		UndoableGame game = makePlay("P.#");
		Food food = (Food) game.getBoard().spriteAt(1, 0);
		Player player = game.getPlayer();

		game.movePlayer(Direction.RIGHT);

		Tile newTile = tileAt(game, 1, 0);
		Assert.assertEquals("Food added", food.getPoints(), player.getPoints());
		Assert.assertEquals("Player moved", newTile.topSprite(), player);
		Assert.assertFalse("Food gone", newTile.containsSprite(food));

		game.undo();

		Tile originalTile = tileAt(game, 0, 0);
		Assert.assertEquals("Food removed", 0, player.getPoints());
		Assert.assertEquals("Player moved back", originalTile.topSprite(),
				player);
		Assert.assertTrue("Food back", newTile.containsSprite(food));
	}

	/**
	 * Test situation that player moves to a food cell and that an undo reverts it all.
	 * 
	 * @throws FactoryException
	 *             Can't happen.
	 */
	@Override
	@Test
	public void testC6_GhostMovesToFood() throws FactoryException {
		UndoableGame game = makePlay("G.#");
		Ghost theGhost = (Ghost) game.getBoard().spriteAt(0, 0);

		game.moveGhost(theGhost, Direction.RIGHT);
		Assert.assertEquals("Ghost moved", tileAt(game, 1, 0),
				theGhost.getTile());

		game.moveGhost(theGhost, Direction.LEFT);
		Assert.assertEquals(SpriteType.FOOD, game.getBoard().spriteTypeAt(1, 0));

		/* We expect no change, ghosts don't create new states upon movement */
		Assert.assertEquals("Ghost stayed at the same position",
				tileAt(game, 0, 0), theGhost.getTile());
		Assert.assertEquals(SpriteType.FOOD, game.getBoard().spriteTypeAt(1, 0));
	}
}
