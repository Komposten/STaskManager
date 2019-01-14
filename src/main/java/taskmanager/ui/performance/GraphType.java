package taskmanager.ui.performance;

import java.awt.Color;

public enum GraphType {
  Memory(new Color(139, 18, 174), "Memory"),
  Cpu(new Color(17, 125, 187), "Cpu"),
  Network(new Color(167, 79, 1), "Network"),
  Disk(new Color(77, 166, 12), "Disk");
  
  public Color color;
  public String header;
  
  private GraphType(Color color, String header) {
    this.color = color;
    this.header = header;
  }
}