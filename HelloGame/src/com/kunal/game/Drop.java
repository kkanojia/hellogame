package com.badlogic.drop;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
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

public class Drop implements ApplicationListener {
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture texture;
	private Sprite sprite;
	private Texture dropImage;
	private Texture bucketImage;
	private Sound dropSound;
	private Music rainMusic;
	private Rectangle bucket;
	private long lastDropTime;
	private Array<Rectangle> raindrops;
	private int currAngle;

	@Override
	public void create() {
		// load the images for the droplet and the bucket, 64x64 pixels each
		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));
j
		// load the drop sound effect and the rain background "music"
		// dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("tiger.mp3"));

		// start the playback of the background music immediately
		rainMusic.setLooping(true);
		rainMusic.play();
		
		sprite = new Sprite(bucketImage);

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		batch = new SpriteBatch();

		bucket = new Rectangle();
		bucket.x = 800 / 2 - 64 / 2;
		bucket.y = (480 / 2) - (64 / 2);
		bucket.width = 64;
		bucket.height = 64;

		raindrops = new Array<Rectangle>();
		spawnRaindrop();

	}

	@Override
	public void dispose() {
		dropImage.dispose();
		bucketImage.dispose();
		// dropSound.dispose();
		rainMusic.dispose();
		batch.dispose();
	}

	@Override
	public void render() {
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
				// dropSound.play();
				iter.remove();
			}
		}

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
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
