package com.example.pacman;

import org.teavm.flavour.templates.BindTemplate;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLImageElement;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.platform.Platform;
import org.teavm.platform.PlatformRunnable;

import java.util.Random;

@BindTemplate("index.html")
public class Main {
    private static HTMLCanvasElement canvas;
    private static HTMLImageElement playerImg;
    private static int playerX = 50, playerY = 50;
    private static int[][] maze = {
        {1,1,1,1,1,1,1,1,1,1},
        {1,0,0,0,1,0,0,0,0,1},
        {1,0,1,0,1,0,1,1,0,1},
        {1,0,1,0,0,0,0,1,0,1},
        {1,0,0,0,1,1,0,0,0,1},
        {1,1,1,0,1,1,0,1,1,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,1,1,1,1,1,1,1,1,1}
    };
    private static int cellSize = 40;
    private static int score = 0;

    public static void main(String[] args) {
        canvas = (HTMLCanvasElement) Window.current().getDocument().getElementById("gameCanvas");
        playerImg = (HTMLImageElement) Window.current().getDocument().createElement("img");
        playerImg.setSrc("default-pacman.png"); // fallback

        setupImageUpload();
        setupKeyboard();
        gameLoop();
    }

    private static void setupImageUpload() {
        var input = Window.current().getDocument().getElementById("imageUpload");
        input.addEventListener("change", evt -> {
            var file = input.getFiles().get(0);
            if (file != null) {
                var reader = Platform.getFileReader();
                reader.addLoadListener(e -> {
                    playerImg.setSrc(reader.getResultAsString());
                    draw();
                });
                reader.readAsDataURL(file);
            }
        });
    }

    private static void setupKeyboard() {
        Window.current().getDocument().addEventListener("keydown", evt -> {
            int key = evt.getKeyCode();
            int newX = playerX, newY = playerY;
            if (key == 37) newX -= cellSize; // left
            if (key == 38) newY -= cellSize; // up
            if (key == 39) newX += cellSize; // right
            if (key == 40) newY += cellSize; // down

            int gridX = newX / cellSize, gridY = newY / cellSize;
            if (gridX >= 0 && gridX < 10 && gridY >= 0 && gridY < 8 && maze[gridY][gridX] == 0) {
                playerX = newX;
                playerY = newY;
                collectDot(gridX, gridY);
            }
            draw();
        });
    }

    private static void collectDot(int x, int y) {
        if (maze[y][x] == 0) {
            maze[y][x] = 2; // collected
            score += 10;
            Window.current().getDocument().getElementById("score").setInnerHTML("Score: " + score);
        }
    }

    private static void draw() {
        var ctx = canvas.getContext("2d");
        ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw maze
        for (int y = 0; y < maze.length; y++) {
            for (int x = 0; x < maze[0].length; x++) {
                if (maze[y][x] == 1) {
                    ctx.setFillStyle("#0000FF");
                    ctx.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                } else if (maze[y][x] == 0) {
                    ctx.setFillStyle("#FFFF00");
                    ctx.beginPath();
                    ctx.arc(x * cellSize + cellSize/2, y * cellSize + cellSize/2, 5, 0, Math.PI * 2);
                    ctx.fill();
                }
            }
        }

        // Draw player (custom image)
        ctx.drawImage(playerImg, playerX, playerY, cellSize, cellSize);
    }

    private static void gameLoop() {
        draw();
        Platform.getAnimationFrame().request(new PlatformRunnable() {
            @Override
            public void run() {
                gameLoop();
            }
        });
    }
}
