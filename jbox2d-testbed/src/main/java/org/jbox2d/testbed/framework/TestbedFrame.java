/*******************************************************************************
 * Copyright (c) 2011, Daniel Murphy All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: * Redistributions of source code must retain the
 * above copyright notice, this list of conditions and the following disclaimer. * Redistributions
 * in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 * * Neither the name of the <organization> nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL DANIEL MURPHY BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
/**
 * Created at 4:23:48 PM Jul 17, 2010
 */
package org.jbox2d.testbed.framework;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jbox2d.common.Vec2;
import org.jbox2d.testbed.framework.TestbedModel.ListItem;
import org.jbox2d.testbed.framework.TestbedSetting.SettingsType;

/**
 * The testbed frame. Contains all stuff. Make sure you call {@link #setVisible(boolean)} and
 * {@link #setDefaultCloseOperation(int)}.
 * 
 * @author Daniel Murphy
 */
@SuppressWarnings("serial")
public class TestbedFrame extends JFrame {

  private int currTestIndex;
  private SidePanel side;
  private TestbedModel model;

  public TestbedFrame(final TestbedModel argModel, final TestbedPanel argPanel) {
    super("JBox2D Testbed");
    model = argModel;
    setLayout(new BorderLayout());

    final TestbedModel m = argModel;
    argPanel.addKeyListener(new KeyListener() {
      @Override
      public void keyTyped(KeyEvent e) {
      }

      @Override
      public void keyReleased(KeyEvent e) {
        char key = e.getKeyChar();
        int code = e.getKeyCode();
        if (key != KeyEvent.CHAR_UNDEFINED) {
          m.getKeys()[key] = false;
        }
        m.getCodedKeys()[code] = false;
        if (m.getCurrTest() != null) {
          m.getCurrTest().queueKeyReleased(key, code);
        }
      }

      @Override
      public void keyPressed(KeyEvent e) {
        char key = e.getKeyChar();
        int code = e.getKeyCode();
        if (key != KeyEvent.CHAR_UNDEFINED) {
          m.getKeys()[key] = true;
        }
        m.getCodedKeys()[code] = true;

        if (key == ' ' && m.getCurrTest() != null) {
          m.getCurrTest().lanchBomb();
        } else if (key == '[') {
          lastTest();
        } else if (key == ']') {
          nextTest();
        }
        if (m.getCurrTest() != null) {
          m.getCurrTest().queueKeyPressed(key, code);
        }
      }
    });

    argPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        if (m.getCurrTest() != null) {
          Vec2 pos = new Vec2(e.getX(), e.getY());
          m.getDebugDraw().getScreenToWorldToOut(pos, pos);
          m.getCurrTest().queueMouseUp(pos);
        }
      }

      @Override
      public void mousePressed(MouseEvent e) {
        argPanel.grabFocus();
        if (m.getCurrTest() != null) {
          Vec2 pos = new Vec2(e.getX(), e.getY());
          if (e.getButton() == MouseEvent.BUTTON1) {
            m.getDebugDraw().getScreenToWorldToOut(pos, pos);
            m.getCurrTest().queueMouseDown(pos);
            if (m.getCodedKeys()[KeyEvent.VK_SHIFT]) {
              m.getCurrTest().queueShiftMouseDown(pos);
            }
          }
        }
      }
    });

    argPanel.addMouseMotionListener(new MouseMotionListener() {
      final Vec2 posDif = new Vec2();
      final Vec2 pos = new Vec2();
      final Vec2 pos2 = new Vec2();

      public void mouseDragged(MouseEvent e) {
        pos.set(e.getX(), e.getY());

        if (e.getButton() == MouseEvent.BUTTON3) {
          posDif.set(m.getMouse());
          m.setMouse(pos);
          posDif.subLocal(pos);
          m.getDebugDraw().getViewportTranform().getScreenVectorToWorld(posDif, posDif);
          m.getDebugDraw().getViewportTranform().getCenter().addLocal(posDif);
          if (m.getCurrTest() != null) {
            m.getCurrTest().cachedCameraX = m.getDebugDraw().getViewportTranform().getCenter().x;
            m.getCurrTest().cachedCameraY = m.getDebugDraw().getViewportTranform().getCenter().y;
          }
        }
        if (m.getCurrTest() != null) {
          m.setMouse(pos);
          m.getDebugDraw().getScreenToWorldToOut(pos, pos);
          m.getCurrTest().queueMouseMove(pos);
        }
      }

      @Override
      public void mouseMoved(MouseEvent e) {
        pos2.set(e.getX(), e.getY());
        m.setMouse(pos2);
        if (m.getCurrTest() != null) {
          m.getDebugDraw().getScreenToWorldToOut(pos2, pos2);
          m.getCurrTest().queueMouseMove(pos2);
        }
      }
    });

    add((Component)argPanel, "Center");
    side = new SidePanel(argModel);
    side.setMain(this);
    add(new JScrollPane(side), "East");
    pack();

    currTestIndex = 0;
    side.tests.setSelectedIndex(0);
    side.actionPerformed(null);
  }

  public void nextTest() {
    int index = currTestIndex + 1;
    index %= model.getTestsSize();

    while (!model.isTestAt(index) && index < model.getTestsSize() - 1) {
      index++;
    }
    if (model.isTestAt(index)) {
      side.tests.setSelectedIndex(index);
    }
  }

  public void saveTest() {
    model.getCurrTest().save();
  }

  public void loadTest() {
    model.getCurrTest().load();
  }

  public void lastTest() {
    int index = currTestIndex - 1;
    index = (index < 0) ? index + model.getTestsSize() : index;

    while (!model.isTestAt(index) && index > 0) {
      index--;
    }
    if (model.isTestAt(index)) {
      side.tests.setSelectedIndex(index);
    }
  }

  public void resetTest() {
    model.getCurrTest().reset();
  }

  public void testChanged(int argIndex) {
    if (argIndex == -1) {
      return;
    }
    while (!model.isTestAt(argIndex)) {
      if (argIndex + 1 < model.getTestsSize()) {
        argIndex++;
      } else {
        return;
      }
    }
    side.tests.setSelectedIndex(argIndex);

    currTestIndex = argIndex;
    TestbedTest test = model.getTestAt(argIndex);
    if (model.getCurrTest() != test) {
      model.setCurrTest(model.getTestAt(argIndex));
      side.saveButton.setEnabled(test.isSaveLoadEnabled());
      side.loadButton.setEnabled(test.isSaveLoadEnabled());
    }
  }
}

/**
 * quick hackup of a side panel
 * 
 * @author Daniel Murphy
 */
@SuppressWarnings("serial")
class SidePanel extends JPanel implements ChangeListener, ActionListener {

  private static final String SETTING_TAG = "settings";
  private static final String LABEL_TAG = "label";

  final TestbedModel model;
  TestbedFrame main;

  public JComboBox tests;

  private JButton pauseButton = new JButton("Pause");
  private JButton stepButton = new JButton("Step");
  private JButton resetButton = new JButton("Reset");
  private JButton quitButton = new JButton("Quit");

  public JButton saveButton = new JButton("Save");
  public JButton loadButton = new JButton("Load");

  public SidePanel(TestbedModel argModel) {
    model = argModel;
    initComponents();
    addListeners();
  }

  public void setMain(TestbedFrame argMain) {
    main = argMain;
  }

  public void initComponents() {
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    TestbedSettings settings = model.getSettings();

    JPanel top = new JPanel();
    top.setLayout(new GridLayout(0, 1));
    top.setBorder(BorderFactory.createCompoundBorder(new EtchedBorder(EtchedBorder.LOWERED),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    tests = new JComboBox(model.getComboModel());
    tests.setMaximumRowCount(30);
    tests.setMaximumSize(new Dimension(250, 20));
    tests.addActionListener(this);
    tests.setRenderer(new ListCellRenderer() {
      JLabel categoryLabel = null;
      JLabel testLabel = null;

      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index,
          boolean isSelected, boolean cellHasFocus) {
        ListItem item = (ListItem) value;

        if (item.isCategory()) {
          if (categoryLabel == null) {
            categoryLabel = new JLabel();
            categoryLabel.setOpaque(true);
            categoryLabel.setBackground(new Color(.5f, .5f, .6f));
            categoryLabel.setForeground(Color.white);
            categoryLabel.setHorizontalAlignment(SwingConstants.CENTER);
            categoryLabel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
          }
          categoryLabel.setText(item.category);
          return categoryLabel;
        } else {
          if (testLabel == null) {
            testLabel = new JLabel();
            testLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 1, 0));
          }

          testLabel.setText(item.test.getTestName());

          if (isSelected) {
            testLabel.setBackground(list.getSelectionBackground());
            testLabel.setForeground(list.getSelectionForeground());
          } else {
            testLabel.setBackground(list.getBackground());
            testLabel.setForeground(list.getForeground());
          }
          return testLabel;
        }
      }
    });
    JPanel testsp = new JPanel();
    testsp.setLayout(new GridLayout(1, 2));
    testsp.add(new JLabel("Choose a test:"));
    testsp.add(tests);

    top.add(tests);

    addSettings(top, settings, SettingsType.DRAWING);

    add(top, "North");

    JPanel middle = new JPanel();
    middle.setLayout(new GridLayout(0, 1));
    middle.setBorder(BorderFactory.createCompoundBorder(new EtchedBorder(EtchedBorder.LOWERED),
        BorderFactory.createEmptyBorder(5, 10, 5, 10)));

    addSettings(middle, settings, SettingsType.ENGINE);

    add(middle, "Center");

    pauseButton.setAlignmentX(CENTER_ALIGNMENT);
    stepButton.setAlignmentX(CENTER_ALIGNMENT);
    resetButton.setAlignmentX(CENTER_ALIGNMENT);
    saveButton.setAlignmentX(CENTER_ALIGNMENT);
    loadButton.setAlignmentX(CENTER_ALIGNMENT);
    quitButton.setAlignmentX(CENTER_ALIGNMENT);

    Box buttonGroups = Box.createHorizontalBox();
    JPanel buttons1 = new JPanel();
    buttons1.setLayout(new GridLayout(0, 1));
    buttons1.add(resetButton);

    JPanel buttons2 = new JPanel();
    buttons2.setLayout(new GridLayout(0, 1));
    buttons2.add(pauseButton);
    buttons2.add(stepButton);

    JPanel buttons3 = new JPanel();
    buttons3.setLayout(new GridLayout(0, 1));
    buttons3.add(saveButton);
    buttons3.add(loadButton);
    buttons3.add(quitButton);

    buttonGroups.add(buttons1);
    buttonGroups.add(buttons2);
    buttonGroups.add(buttons3);

    add(buttonGroups, "South");
  }

  public void addListeners() {
    pauseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (model.getSettings().pause) {
          model.getSettings().pause = false;
          pauseButton.setText("Pause");
        } else {
          model.getSettings().pause = true;
          pauseButton.setText("Resume");
        }
      }
    });

    stepButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        model.getSettings().singleStep = true;
        if (!model.getSettings().pause) {
          model.getSettings().pause = true;
          pauseButton.setText("Resume");
        }
      }
    });

    resetButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        main.resetTest();
      }
    });

    quitButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });

    saveButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        main.saveTest();
      }
    });

    loadButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        main.loadTest();
      }
    });
  }

  private void addSettings(JPanel argPanel, TestbedSettings argSettings, SettingsType argIgnore) {
    for (TestbedSetting setting : argSettings.getSettings()) {
      if (setting.settingsType == argIgnore) {
        continue;
      }
      switch (setting.constraintType) {
        case RANGE:
          JLabel text = new JLabel(setting.name + ": " + setting.value);
          JSlider slider = new JSlider(setting.min, setting.max, setting.value);
          slider.setMaximumSize(new Dimension(200, 20));
          slider.addChangeListener(this);
          slider.setName(setting.name);
          slider.putClientProperty(SETTING_TAG, setting);
          slider.putClientProperty(LABEL_TAG, text);
          argPanel.add(text);
          argPanel.add(slider);
          break;
        case BOOLEAN:
          JCheckBox checkbox = new JCheckBox(setting.name);
          checkbox.setSelected(setting.enabled);
          checkbox.addChangeListener(this);
          checkbox.putClientProperty(SETTING_TAG, setting);
          argPanel.add(checkbox);
          break;
      }
    }
  }

  public void stateChanged(ChangeEvent e) {
    JComponent component = (JComponent) e.getSource();
    TestbedSetting setting = (TestbedSetting) component.getClientProperty(SETTING_TAG);

    switch (setting.constraintType) {
      case BOOLEAN:
        JCheckBox box = (JCheckBox) e.getSource();
        setting.enabled = box.isSelected();
        break;
      case RANGE:
        JSlider slider = (JSlider) e.getSource();
        setting.value = slider.getValue();
        JLabel label = (JLabel) slider.getClientProperty(LABEL_TAG);
        label.setText(setting.name + ": " + setting.value);
        break;
    }
  }

  public void actionPerformed(ActionEvent e) {
    main.testChanged(tests.getSelectedIndex());
  }
}