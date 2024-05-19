package com.gggeralt.game.screens.gameScreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameScreen extends ScreenAdapter {

    private Map map;
    private Texture brickTexture;
    private Texture backgroundTexture;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private CollisionElement hero;
    private Rectangle heroBounds;
    private List<Rectangle> brickBounds;
    private boolean canMove;
    private boolean isJumping = false;
    private boolean isSecondJumpAvailable = false;
    private float jumpVelocity = 0;
    private final float gravity = -0.5f;
    private final float jump_force = 10;
    private ArrayList<Texture> images;
    private int textureNumber=0;
    private int animationTimeout=0;

    public GameScreen() {
        map = new Map();
        batch = new SpriteBatch();
        brickTexture = new Texture("bricks.png");
        backgroundTexture = new Texture("background.png");
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        images = new ArrayList<>(Arrays.asList(
                new Texture("bikerIdle.png"),
                new Texture("bikerIdle11.png"),
                new Texture("bikerIdle12.png"),
                new Texture("bikerIdle13.png")
        ));
        this.hero = new CollisionElement(images.get(textureNumber), 0, 64);
        heroBounds = new Rectangle(
                hero.getX(),
                hero.getY(),
                hero.getTexture().getWidth(),
                hero.getTexture().getHeight()
        );
        heroBounds.setPosition(hero.getX(), hero.getY());
        brickBounds = new ArrayList<>();
        for (Vector2 coord : map.getCoords()) {
            brickBounds.add(new Rectangle(coord.x, coord.y, brickTexture.getWidth(), brickTexture.getHeight()));
        }
        canMove = true;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        float backgroundX = camera.position.x - camera.viewportWidth / 2;
        float backgroundY = camera.position.y - camera.viewportHeight / 2;
        batch.begin();
        batch.draw(backgroundTexture, backgroundX, backgroundY, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        handleInput();

        updateJump();

        batch.begin();
        renderMap();
        renderHero();
        batch.end();
    }

    private void renderMap() {
        List<Vector2> coords = map.getCoords();
        for (Vector2 coord : coords) {
            batch.draw(brickTexture, coord.x, coord.y);
        }
    }

    private void renderHero() {
        batch.draw(hero.getTexture(), hero.getX(), hero.getY());
    }

    private void handleInput() {

        if(animationTimeout<3){
            animationTimeout++;
        }else{
            animationTimeout=0;
        }

        if(animationTimeout==2&&textureNumber<3){
            textureNumber++;
        }else if(animationTimeout==2&&textureNumber==3){
            textureNumber=0;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            heroBounds.setPosition(hero.getX() + 5, hero.getY());
            for (Rectangle brickBound : brickBounds) {
                if (heroBounds.overlaps(brickBound)) {
                    canMove = false;
                    System.out.println(canMove);
                    break;
                }
            }
            heroBounds.setPosition(hero.getX(), hero.getY());
            if (canMove && camera.position.x + camera.viewportWidth / 2 < 6400 + camera.viewportWidth) {
                camera.position.x += 5;
                hero.setX(camera.position.x - camera.viewportWidth / 2);
                if(animationTimeout==2)
                    hero.setTexture(images.get(textureNumber));
            }
            canMove = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            heroBounds.setPosition(hero.getX() - 5, hero.getY());

            for (Rectangle brickBound : brickBounds) {
                if (heroBounds.overlaps(brickBound)) {
                    canMove = false;
                    System.out.println(canMove + " w tyl");
                    break;
                }
            }
            heroBounds.setPosition(hero.getX(), hero.getY());
            if (canMove && camera.position.x - camera.viewportWidth / 2 > 0) {
                camera.position.x -= 5;
                hero.setX(camera.position.x - camera.viewportWidth / 2);
                if(animationTimeout==2)
                    hero.setTexture(images.get(textureNumber));

            }
            canMove = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {

            if (!isJumping) {
                jump();
            } else if (isSecondJumpAvailable) {
                secondJump();
            }
        }
    }

    private void jump() {
        isJumping = true;
        isSecondJumpAvailable = true;
        jumpVelocity = jump_force;
    }


    private void secondJump() {
        isSecondJumpAvailable = false;
        jumpVelocity = jump_force;
    }

    private void updateJump() {
        if (isJumping) {
            boolean canMoveUp = true;
            boolean canMoveDown = true;

            if (jumpVelocity > 0) {
                heroBounds.setPosition(hero.getX(), hero.getY() + jumpVelocity);
                for (Rectangle brickBound : brickBounds) {
                    if (heroBounds.overlaps(brickBound)) {
                        canMoveUp = false;
                        jumpVelocity = 0;
                        break;
                    }
                }
            }

            if (jumpVelocity < 0) {
                heroBounds.setPosition(hero.getX(), hero.getY() + jumpVelocity);
                for (Rectangle brickBound : brickBounds) {
                    if (heroBounds.overlaps(brickBound)) {
                        canMoveDown = false;
                        jumpVelocity = 0;
                        hero.setY(brickBound.y + brickBound.height);
                        isJumping = false;
                        isSecondJumpAvailable = false;
                        break;
                    }
                }
            }

            heroBounds.setPosition(hero.getX(), hero.getY());

            if (canMoveUp || canMoveDown) {
                hero.moveBy(0, jumpVelocity);
                jumpVelocity += gravity;
            }

            if (hero.getY() <= -32) {
                hero.setY(-32);
                isJumping = false;
                jumpVelocity = 0;
                isSecondJumpAvailable = false;
            }
        } else {
            boolean onGround = false;
            heroBounds.setPosition(hero.getX(), hero.getY() - 1);
            for (Rectangle brickBound : brickBounds) {
                if (heroBounds.overlaps(brickBound)) {
                    onGround = true;
                    break;
                }
            }

            heroBounds.setPosition(hero.getX(), hero.getY());

            if (!onGround && hero.getY() > -32) {
                isJumping = true;
                jumpVelocity = 0;
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        brickTexture.dispose();
        hero.getTexture().dispose();
    }


}
