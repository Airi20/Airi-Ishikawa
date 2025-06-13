import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class TrussForceApp {
    private JFrame frame;
    private JTable nodeTable, memberTable;
    private DefaultTableModel nodeTableModel, memberTableModel;
    private DrawingPanel drawingPanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TrussForceApp().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("TrussForce");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // ノードテーブルモデルとテーブル作成
        nodeTableModel = new DefaultTableModel(new Object[]{"ID", "X", "Y", "Support", "Fx", "Fy"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0; // ID列は編集不可（行番号固定）
            }
        };
        nodeTable = new JTable(nodeTableModel);
        JButton addNodeBtn = new JButton("+Node");
        addNodeBtn.addActionListener(e -> {
            int id = nodeTableModel.getRowCount();
            nodeTableModel.addRow(new Object[]{id, "", "", "", "0", "0"});
        });

        // 部材テーブルモデルとテーブル作成
        memberTableModel = new DefaultTableModel(new Object[]{"Start", "End"}, 0);
        memberTable = new JTable(memberTableModel);
        JButton addMemberBtn = new JButton("+Member");
        addMemberBtn.addActionListener(e -> memberTableModel.addRow(new Object[]{"", ""}));

        // 計算ボタン
        JButton calcButton = new JButton("計算");
        calcButton.addActionListener(this::calculate);

        // パネルレイアウト
        JPanel inputPanel = new JPanel(new GridLayout(1, 2));
        JPanel nodePanel = new JPanel(new BorderLayout());
        nodePanel.add(new JLabel("Nodes"), BorderLayout.NORTH);
        nodePanel.add(new JScrollPane(nodeTable), BorderLayout.CENTER);
        nodePanel.add(addNodeBtn, BorderLayout.SOUTH);

        JPanel memberPanel = new JPanel(new BorderLayout());
        memberPanel.add(new JLabel("Members"), BorderLayout.NORTH);
        memberPanel.add(new JScrollPane(memberTable), BorderLayout.CENTER);
        memberPanel.add(addMemberBtn, BorderLayout.SOUTH);

        inputPanel.add(nodePanel);
        inputPanel.add(memberPanel);

        drawingPanel = new DrawingPanel();

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(drawingPanel, BorderLayout.CENTER);
        frame.add(calcButton, BorderLayout.SOUTH);

        frame.setSize(1000, 800);
        frame.setVisible(true);
    }

    private void calculate(ActionEvent e) {
        try {
            // テーブルからデータ読み込み
            List<Node> nodes = new ArrayList<>();
            for (int i = 0; i < nodeTableModel.getRowCount(); i++) {
                int id = i; // IDは行番号固定
                double x = parseDoubleSafe(nodeTableModel.getValueAt(i, 1));
                double y = parseDoubleSafe(nodeTableModel.getValueAt(i, 2));
                String support = ((String) nodeTableModel.getValueAt(i, 3));
                if (support == null) support = "";
                double fx = parseDoubleSafe(nodeTableModel.getValueAt(i, 4));
                double fy = parseDoubleSafe(nodeTableModel.getValueAt(i, 5));
                nodes.add(new Node(id, x, y, support.trim().toLowerCase(), fx, fy));
            }

            List<Member> members = new ArrayList<>();
            for (int i = 0; i < memberTableModel.getRowCount(); i++) {
                int start = (int) parseDoubleSafe(memberTableModel.getValueAt(i, 0));
                int end = (int) parseDoubleSafe(memberTableModel.getValueAt(i, 1));
                members.add(new Member(start, end));
            }

            TrussForceSolver solver = new TrussForceSolver(nodes, members);
            solver.solve();

            drawingPanel.setData(solver);
            drawingPanel.repaint();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "入力データに誤りがあります。\n" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private double parseDoubleSafe(Object val) {
        if (val == null) return 0;
        try {
            return Double.parseDouble(val.toString());
        } catch (Exception ex) {
            return 0;
        }
    }

    class DrawingPanel extends JPanel {
        private TrussForceSolver solver;

        public void setData(TrussForceSolver solver) {
            this.solver = solver;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (solver == null) return;
            Graphics2D g2 = (Graphics2D) g;
            solver.draw(g2, getWidth(), getHeight());
        }
    }

    // ノードクラス
    static class Node {
        int id;
        double x, y;
        String support; // "pin", "rollerx", "rollery", "none"など
        double fx, fy;

        public Node(int id, double x, double y, String support, double fx, double fy) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.support = support;
            this.fx = fx;
            this.fy = fy;
        }
    }

    // 部材クラス
    static class Member {
        int startNodeId;
        int endNodeId;

        public Member(int startNodeId, int endNodeId) {
            this.startNodeId = startNodeId;
            this.endNodeId = endNodeId;
        }
    }

    // 計算・描画クラス
    static class TrussForceSolver {
        List<Node> nodes;
        List<Member> members;

        Map<Integer, Double> reactionsX = new HashMap<>();
        Map<Integer, Double> reactionsY = new HashMap<>();
        Map<Member, Double> memberForces = new HashMap<>();

        public TrussForceSolver(List<Node> nodes, List<Member> members) {
            this.nodes = nodes;
            this.members = members;
        }

        public void solve() {
            // 反力自由度割り当て
            List<Integer> reactionNodeIds = new ArrayList<>();
            Map<Integer, Integer> reactionXIndex = new HashMap<>();
            Map<Integer, Integer> reactionYIndex = new HashMap<>();
            int reactionCount = 0;

            for (Node n : nodes) {
                String s = n.support.toLowerCase();
                if (s.contains("pin")) {
                    reactionXIndex.put(n.id, reactionCount++);
                    reactionYIndex.put(n.id, reactionCount++);
                    reactionNodeIds.add(n.id);
                } else if (s.contains("rollerx")) {
                    reactionXIndex.put(n.id, reactionCount++);
                    reactionNodeIds.add(n.id);
                } else if (s.contains("rollery")) {
                    reactionYIndex.put(n.id, reactionCount++);
                    reactionNodeIds.add(n.id);
                }
            }

            int varCount = reactionCount + members.size();
            int eqCount = nodes.size() * 2;

            double[][] A = new double[eqCount][varCount];
            double[] b = new double[eqCount];

            Map<Integer, Integer> nodeRowMapX = new HashMap<>();
            Map<Integer, Integer> nodeRowMapY = new HashMap<>();
            for (int i = 0; i < nodes.size(); i++) {
                nodeRowMapX.put(nodes.get(i).id, i * 2);
                nodeRowMapY.put(nodes.get(i).id, i * 2 + 1);
            }

            // 反力成分セット
            for (Node n : nodes) {
                int rxIdx = reactionXIndex.getOrDefault(n.id, -1);
                int ryIdx = reactionYIndex.getOrDefault(n.id, -1);
                int rowX = nodeRowMapX.get(n.id);
                int rowY = nodeRowMapY.get(n.id);

                if (rxIdx >= 0) {
                    A[rowX][rxIdx] = 1.0;
                }
                if (ryIdx >= 0) {
                    A[rowY][ryIdx] = 1.0;
                }
            }

            // 部材力の係数セット
            for (int i = 0; i < members.size(); i++) {
                Member m = members.get(i);
                Node n1 = getNodeById(m.startNodeId);
                Node n2 = getNodeById(m.endNodeId);
                if (n1 == null || n2 == null) continue;

                double dx = n2.x - n1.x;
                double dy = n2.y - n1.y;
                double length = Math.sqrt(dx * dx + dy * dy);
                if (length < 1e-12) continue;

                double cos = dx / length;
                double sin = dy / length;

                int rowX1 = nodeRowMapX.get(n1.id);
                int rowY1 = nodeRowMapY.get(n1.id);
                int rowX2 = nodeRowMapX.get(n2.id);
                int rowY2 = nodeRowMapY.get(n2.id);

                A[rowX1][reactionCount + i] = cos;
                A[rowY1][reactionCount + i] = sin;
                A[rowX2][reactionCount + i] = -cos;
                A[rowY2][reactionCount + i] = -sin;
            }

            // 右辺ベクトル設定（外力の符号は注意）
            for (Node n : nodes) {
                int rowX = nodeRowMapX.get(n.id);
                int rowY = nodeRowMapY.get(n.id);
                b[rowX] = -n.fx;
                b[rowY] = -n.fy;
            }

            // 連立方程式を解く
            double[] x = gaussJordan(A, b);

            if (x == null) {
                JOptionPane.showMessageDialog(null, "連立方程式の解が見つかりませんでした。入力を確認してください。");
                return;
            }

            // 反力値セット
            reactionsX.clear();
            reactionsY.clear();
            for (int i = 0; i < reactionCount; i++) {
                int nodeId = -1;
                for (Map.Entry<Integer, Integer> e : reactionXIndex.entrySet()) {
                    if (e.getValue() == i) {
                        nodeId = e.getKey();
                        reactionsX.put(nodeId, x[i]);
                        break;
                    }
                }
                for (Map.Entry<Integer, Integer> e : reactionYIndex.entrySet()) {
                    if (e.getValue() == i) {
                        nodeId = e.getKey();
                        reactionsY.put(nodeId, x[i]);
                        break;
                    }
                }
            }

            // 部材力セット
            memberForces.clear();
            for (int i = 0; i < members.size(); i++) {
                memberForces.put(members.get(i), x[reactionCount + i]);
            }
        }

        private Node getNodeById(int id) {
            for (Node n : nodes) {
                if (n.id == id) return n;
            }
            return null;
        }

        // ガウス・ジョルダン法による連立方程式解法
        private double[] gaussJordan(double[][] A, double[] b) {
            int n = A.length;
            int m = A[0].length;
            double[][] mat = new double[n][m + 1];
            for (int i = 0; i < n; i++) {
                System.arraycopy(A[i], 0, mat[i], 0, m);
                mat[i][m] = b[i];
            }

            int row = 0;
            for (int col = 0; col < m && row < n; col++) {
                int sel = row;
                for (int i = row + 1; i < n; i++) {
                    if (Math.abs(mat[i][col]) > Math.abs(mat[sel][col])) sel = i;
                }
                if (Math.abs(mat[sel][col]) < 1e-12) continue;
                double[] tmp = mat[sel];
                mat[sel] = mat[row];
                mat[row] = tmp;

                double div = mat[row][col];
                for (int j = col; j <= m; j++) mat[row][j] /= div;

                for (int i = 0; i < n; i++) {
                    if (i != row) {
                        double mul = mat[i][col];
                        for (int j = col; j <= m; j++) {
                            mat[i][j] -= mat[row][j] * mul;
                        }
                    }
                }
                row++;
            }

            double[] x = new double[m];
            for (int i = 0; i < m; i++) x[i] = 0;

            for (int i = 0; i < n; i++) {
                int pivot = -1;
                for (int j = 0; j < m; j++) {
                    if (Math.abs(mat[i][j] - 1.0) < 1e-10) {
                        pivot = j;
                        break;
                    }
                }
                if (pivot == -1) continue;
                x[pivot] = mat[i][m];
            }

            return x;
        }

        // 描画関数
        public void draw(Graphics2D g2, int width, int height) {
            if (nodes.isEmpty()) return;

            // 座標範囲計算
            double minX = nodes.stream().mapToDouble(n -> n.x).min().orElse(0);
            double maxX = nodes.stream().mapToDouble(n -> n.x).max().orElse(1);
            double minY = nodes.stream().mapToDouble(n -> n.y).min().orElse(0);
            double maxY = nodes.stream().mapToDouble(n -> n.y).max().orElse(1);

            double margin = 50;
            double scaleX = (width - 2 * margin) / (maxX - minX);
            double scaleY = (height - 2 * margin) / (maxY - minY);
            double scale = Math.min(scaleX, scaleY);

            // 座標変換関数
            java.util.function.Function<Double, Integer> transX = x -> (int) ((x - minX) * scale + margin);
            java.util.function.Function<Double, Integer> transY = y -> (int) ((maxY - y) * scale + margin);

            // 背景クリア
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, width, height);

            // 部材描画
            g2.setColor(Color.BLACK);
            for (Member m : members) {
                Node n1 = getNodeById(m.startNodeId);
                Node n2 = getNodeById(m.endNodeId);
                if (n1 == null || n2 == null) continue;
                int x1 = transX.apply(n1.x);
                int y1 = transY.apply(n1.y);
                int x2 = transX.apply(n2.x);
                int y2 = transY.apply(n2.y);
                g2.drawLine(x1, y1, x2, y2);

                // 部材力表示（数値と色分け）
                Double force = memberForces.get(m);
                if (force != null) {
                    String forceStr = String.format("%.2f", force);
                    int midX = (x1 + x2) / 2;
                    int midY = (y1 + y2) / 2;
                    if (force > 0) {
                        g2.setColor(Color.RED); // 引張
                    } else {
                        g2.setColor(Color.BLUE); // 圧縮
                    }
                    g2.drawString(forceStr, midX + 5, midY - 5);
                    g2.setColor(Color.BLACK);
                }
            }

            // ノード描画と力表示
            for (Node n : nodes) {
                int x = transX.apply(n.x);
                int y = transY.apply(n.y);
                g2.setColor(Color.GREEN.darker());
                g2.fillOval(x - 6, y - 6, 12, 12);
                g2.setColor(Color.BLACK);
                g2.drawString("N" + n.id, x + 6, y - 6);

                // 支点マーク
                if (n.support.contains("pin")) {
                    g2.drawOval(x - 10, y - 10, 20, 20);
                    g2.drawLine(x - 10, y + 10, x + 10, y + 10);
                } else if (n.support.contains("rollerx")) {
                    g2.drawOval(x - 10, y - 10, 20, 20);
                    g2.drawLine(x - 10, y + 10, x + 10, y + 10);
                    g2.drawLine(x, y + 10, x, y + 15);
                } else if (n.support.contains("rollery")) {
                    g2.drawOval(x - 10, y - 10, 20, 20);
                    g2.drawLine(x - 10, y + 10, x + 10, y + 10);
                    g2.drawLine(x + 10, y, x + 15, y);
                }

                // 反力表示（赤矢印）
                Double rx = reactionsX.get(n.id);
                Double ry = reactionsY.get(n.id);
                if (rx != null && Math.abs(rx) > 1e-6) {
                    drawArrow(g2, x, y, (int) (10 * Math.signum(rx)), 0, Color.RED);
                    g2.drawString(String.format("Rx=%.2f", rx), x + 12, y - 5);
                }
                if (ry != null && Math.abs(ry) > 1e-6) {
                    drawArrow(g2, x, y, 0, (int) (-10 * Math.signum(ry)), Color.RED);
                    g2.drawString(String.format("Ry=%.2f", ry), x + 12, y + 15);
                }

                // 荷重表示（青矢印）
                if (Math.abs(n.fx) > 1e-6) {
                    drawArrow(g2, x, y, (int) (10 * Math.signum(n.fx)), 0, Color.BLUE);
                    g2.drawString(String.format("Fx=%.2f", n.fx), x - 40, y - 5);
                }
                if (Math.abs(n.fy) > 1e-6) {
                    drawArrow(g2, x, y, 0, (int) (-10 * Math.signum(n.fy)), Color.BLUE);
                    g2.drawString(String.format("Fy=%.2f", n.fy), x - 40, y + 15);
                }
            }
        }

        private void drawArrow(Graphics2D g2, int x, int y, int dx, int dy, Color color) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(x, y, x + dx, y + dy);

            double angle = Math.atan2(dy, dx);
            int arrowSize = 6;

            int x2 = x + dx;
            int y2 = y + dy;

            int xArrow1 = (int) (x2 - arrowSize * Math.cos(angle - Math.PI / 6));
            int yArrow1 = (int) (y2 - arrowSize * Math.sin(angle - Math.PI / 6));
            int xArrow2 = (int) (x2 - arrowSize * Math.cos(angle + Math.PI / 6));
            int yArrow2 = (int) (y2 - arrowSize * Math.sin(angle + Math.PI / 6));

            Polygon arrowHead = new Polygon();
            arrowHead.addPoint(x2, y2);
            arrowHead.addPoint(xArrow1, yArrow1);
            arrowHead.addPoint(xArrow2, yArrow2);

            g2.fill(arrowHead);
        }
    }
}
