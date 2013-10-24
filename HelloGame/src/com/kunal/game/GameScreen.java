package com.kunal.game;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class GameScreen implements Screen {
	final Drop game;

	Texture dropImage;
	Texture bucketImage;
	Sound dropSound;
	Music rainMusic;
	OrthographicCamera camera;
	Rectangle bucket;
	Array<Rectangle> raindrops;
	long lastDropTime;
	int dropsGathered;
	private SpriteBatch batch;
	private Sprite sprite;

	public GameScreen(final Drop gam) {
		this.game = gam;

		// load the images for the droplet and the bucket, 64x64 pixels each
		dropImage = new Texture(Gdx.files.internal("data/droplet.png"));
		bucketImage = new Texture(Gdx.files.internal("data/bucket.png"));

		// load the drop sound effect and the rain background "music"
		dropSound = Gdx.audio.newSound(Gdx.files.internal("data/drop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("data/rain.mp3"));
		rainMusic.setLooping(true);

		// create the camera and the SpriteBatch
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		sprite = new Sprite(bucketImage);

		// create a Rectangle to logically represent the bucket
		batch = new SpriteBatch();

		bucket = new Rectangle();
		bucket.x = 800 / 2 - 64 / 2;
		bucket.y = (480 / 2) - (64 / 2);
		bucket.width = 64;
		bucket.height = 64;

		raindrops = new Array<Rectangle>();
		spawnRaindrop();

	}

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		float random = MathUtils.random();
		if (random < 0.25) {
			raindrop.x = MathUtils.random(0, 800 - 64);
			raindrop.y = 480;
		} else if (random < 0.50) {
			raindrop.x = MathUtils.random(0, 800 - 64);
			raindrop.y = 0;
		} else if (random < 0.75) {
			raindrop.x = 800;
			raindrop.y = MathUtils.random(0, 480 - 64);
		} else {
			raindrop.x = 0;
			raindrop.y = MathUtils.random(0, 480 - 64);
		}
		raindrop.width = 16;
		raindrop.height = 16;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		camera.update();
		

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		sprite.setPosition(bucket.x, bucket.y);

		if (Gdx.input.isKeyPressed(Keys.UP))
			sprite.setRotation(0);
		else if (Gdx.input.isKeyPressed(Keys.DOWN))
			sprite.setRotation(180);
		else if (Gdx.input.isKeyPressed(Keys.LEFT))
			sprite.setRotation(90);
		else if (Gdx.input.isKeyPressed(Keys.RIGHT))
			sprite.setRotation(270);

		sprite.draw(batch);
		
		game.font.draw(batch, "Drops Collected: " + dropsGathered, 0, 480);

		for (Rectangle raindrop : raindrops) {
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		batch.end();

		// if (Gdx.input.isTouched()) {
		// Vector3 touchPos = new Vector3();
		// touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		// camera.unproject(touchPos);
		// bucket.x = touchPos.x - 64 / 2;
		// }

		if (TimeUtils.nanoTime() - lastDropTime > 1000000000)
			spawnRaindrop();

		Iterator<Rectangle> iter = raindrops.iterator();
		while (iter.hasNext()) {
			Rectangle raindrop = iter.next();

			float slope = (240 - raindrop.y) / (400 - raindrop.x);

			float x = raindrop.x;
			if (raindrop.x > 400) {
				raindrop.x = (float) (x - ((200 * Gdx.graphics.getDeltaTime()) / Math
						.sqrt((1 + (slope * slope)))));
			} else if (raindrop.x < 400) {
				raindrop.x = (float) (x + ((200 * Gdx.graphics.getDeltaTime()) / Math
						.sqrt((1 + (slope * slope)))));
			}

			if (raindrop.y != 240) {
				raindrop.y = raindrop.y + (slope * (raindrop.x - x));
			}

			if (raindrop.overlaps(bucket)) {
				dropsGathered++;
				dropSound.play();
				iter.remove();
			}
		}
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void show() {
		// start the playback of the background music
		// when the screen is shown
		rainMusic.play();
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
	}

}