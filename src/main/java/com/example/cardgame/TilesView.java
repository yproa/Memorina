package com.example.cardgame;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;


class Card {
    Paint p = new Paint();
    int outline = 10;

    public void CardProperties(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Card(int color) {
        this.color = color;
    }

    int color, backColor = Color.DKGRAY, solved = Color.WHITE;
    boolean isOpen = false;
    float x, y, width, height;

    public void draw(Canvas c) {
        if (isOpen) {
            p.setColor(color);
        } else p.setColor(backColor);
        c.drawRect(x+outline,y+outline, x+width - outline, y+height - outline, p);
    }
    public boolean flip (float touch_x, float touch_y) {
        if (touch_x >= x && touch_x <= x + width && touch_y >= y && touch_y <= y + height) {
            isOpen = ! isOpen;
            return true;
        } else return false;
    }
}

public class TilesView extends View {

    int row = 4, col = 4;
    final int PAUSE_LENGTH = 2;
    boolean isOnPauseNow = false;
    int openedCard = 0;
    int matchs=0;

    Card[] openedCards = new Card[2];
    Card[][] cards = new Card[row][col];

    ArrayList<Integer> colors = new ArrayList<Integer>(Arrays.asList(
            Color.rgb(192,192,192), Color.rgb(0,128,0),
            Color.rgb(128,0,128), Color.rgb(0,128,128),
            Color.rgb(255,0,255), Color.rgb(0,0,255),
            Color.rgb(255,255,0), Color.rgb(75,255,255),
            Color.rgb(192,192,192), Color.rgb(0,128,0),
            Color.rgb(128,0,128), Color.rgb(0,128,128),
            Color.rgb(255,0,255), Color.rgb(0,0,255),
            Color.rgb(255,255,0), Color.rgb(75,255,255)
    ));// самый простой алгоритм что пришёл в голову: 8 цветов перечисленные дважды

    public TilesView(Context context) {
        super(context);
    }

    public TilesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                int random = (int) (Math.random() * colors.size());//выбираем случайный из цветов
                cards[i][j] = new Card(colors.get(random));//вписываем на номер этой карты цвет который нарандомили
                colors.remove(random);//удаляем его из массива
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int card_height = canvas.getHeight() / row;
        int card_width = canvas.getWidth() / col;

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {

                int left = j * card_width;
                int top = i * card_height;

                cards[i][j].CardProperties(left, top, card_width, card_height);
                cards[i][j].draw(canvas);

            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN && !isOnPauseNow)
        {
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {

                    if (openedCard == 0) {
                        if (cards[i][j].flip(x, y)) {

                            openedCard++;
                            openedCards[0] = cards[i][j];
                            invalidate();
                            return true;
                        }
                    }

                    if (openedCard == 1) {
                        if (cards[i][j].flip(x, y)) {
                            openedCard++;
                            openedCards[1] = cards[i][j];

                            if (openedCards[0].color == openedCards[1].color) {

                                Toast toast = Toast.makeText(getContext(), "Same card!", Toast.LENGTH_SHORT);
                                toast.show();

                                openedCards[0].backColor = openedCards[0].solved;
                                openedCards[1].backColor = openedCards[1].solved;
                                matchs++;
                                if (matchs>=col*row/2){
                                    Toast wintoast = Toast.makeText(getContext(), "You won!", Toast.LENGTH_SHORT);
                                    wintoast.show();
                                }

                            }
                            invalidate();
                            PauseTask task = new PauseTask();
                            task.execute(PAUSE_LENGTH);
                            isOnPauseNow = true;

                            return true;
                        }
                    }

                }
            }

        }
        return true;
    }

    public void newGame() {//это оказалось сложнее чем ожидалось
    }

    class PauseTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... integers) {
            try {
                Thread.sleep(integers[0] * 1000);
            } catch (InterruptedException e) {}
            return null;
        }



        @Override
        protected void onPostExecute(Void aVoid) {
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    if (cards[i][j].isOpen) {
                        cards[i][j].isOpen = false;
                    }
                }
            }
            openedCard = 0;
            isOnPauseNow = false;
            invalidate();
        }
    }
}