/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/
package jpa.tools.swing;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.TimeUnit;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class StatusBar extends JPanel implements PropertyChangeListener {
    private JProgressBar progressBar;
    private JProgressBar memoryBar;
    private JLabel messageText;
    private TaskProgress task;
    
    public StatusBar() {
        progressBar = new JProgressBar();
        memoryBar = new JProgressBar();
        
        messageText = new JLabel();
        
        setLayout(new GridLayout(1,0));
        add(messageText);
        add(Box.createHorizontalGlue());
        add(memoryBar);
        add(new JLabel("Total " + (Runtime.getRuntime().maxMemory()/1000000) + "MB"));
        add(progressBar);
        MemoryDisplay memory = new MemoryDisplay(memoryBar);
        new Timer(100, memory).start();
    }
    
    public void showMessage(String text) {
        messageText.setText(text);
    }
    
    public void startTimer(long duration, int interval, TimeUnit unit) {
        progressBar.setEnabled(true);
        if (duration > 0) {
            progressBar.setStringPainted(true);
            progressBar.setMaximum(100);
            progressBar.setMinimum(0);
            task = new TaskProgress(unit.toMillis(duration), interval);
            task.addPropertyChangeListener(this);
            task.execute();
        } else {
            progressBar.setStringPainted(false);
            progressBar.setIndeterminate(true);
            task = new TaskProgress(duration, interval);
            task.addPropertyChangeListener(this);
            task.execute();
        }
    }
    
    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {
            progressBar.setValue((Integer)evt.getNewValue());
        } 
    }

    
    public void stopTimer() {
        if (task != null) {
            task.cancel(true);
            task = null;
        }
        progressBar.setIndeterminate(false);
        progressBar.setString("");
        progressBar.setEnabled(false);
    }
    
    /*
     * Emits progress property from a background thread.
     */
    class TaskProgress extends SwingWorker<Void, Integer> {
        private long startTimeInMillis;
        private long _total = 100;
        private int _interval = 100;
        
        public TaskProgress(long total, int interval) {
            _total    = Math.max(total, 1);
            _interval = Math.max(interval, 1);
        }
        @Override
        public Void doInBackground() {
            startTimeInMillis = System.currentTimeMillis();
            long endTimeInMillis = startTimeInMillis + _total;
            long current = System.currentTimeMillis();
            while (current < endTimeInMillis && !isCancelled()) {
                try {
                    current = System.currentTimeMillis();
                    int pctComplete = (int)((100*(current - startTimeInMillis))/_total);
                    setProgress(pctComplete);
                    Thread.sleep(_interval);
                } catch (InterruptedException ignore) {
                }
            }
            return null;
        }
    }
    
    public class MemoryDisplay implements ActionListener {
        JProgressBar bar;
        public MemoryDisplay(JProgressBar bar) {
            this.bar = bar;
            bar.setStringPainted(true);
            bar.setMinimum(0);
            bar.setMaximum(100);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            Runtime jvm = Runtime.getRuntime();
            long totalMemory = jvm.totalMemory();
            long usedMemory = totalMemory-jvm.freeMemory();
            int usedPct = (int)((100*usedMemory)/totalMemory);
            bar.setForeground(getColor(usedPct));
            bar.setValue((int)usedPct);
            bar.setString(usedPct + "% (" + mb(usedMemory) + "/" + mb(totalMemory) + "MB) ");
        }
        
        private long mb(long m) {
            return m/1000000;
        }
        
        Color getColor(int pct) {
            int red = 255*pct/100;
            int green = 255*(100-pct)/100;
            return new Color(red, green, 0);
        }
    }
    
}
