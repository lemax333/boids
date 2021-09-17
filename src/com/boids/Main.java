package com.boids;

import java.awt.*;
import javax.swing.*;

public class Main{

    public static void main(String[] args){

	JFrame frame = new JFrame("Boids");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


	JPanel panel = new JPanel(new BorderLayout());
	GameComponent game = new GameComponent();
	panel.add(game, BorderLayout.CENTER);
	panel.setIgnoreRepaint(true);

	frame.setContentPane(panel);
	frame.pack();
	frame.setLocationRelativeTo(null);
	frame.setResizable(false);
	frame.setIgnoreRepaint(true);
	frame.setVisible(true);

	game.start();
    }
}
