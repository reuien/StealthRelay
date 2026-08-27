from flask import Flask, request, jsonify
import matplotlib.pyplot as plt
import os
from datetime import datetime
import warnings

app = Flask(__name__)

# 设置字体
plt.rcParams['font.sans-serif'] = ['SimHei']
plt.rcParams['axes.unicode_minus'] = False

warnings.filterwarnings("ignore")

@app.route('/plot2', methods=['POST'])
def plot2():
    data = request.json
    try:
        yCountValues2 = data['yCountValues2']
        categories2 = data['categories2']
        statisticValues = data['statisticValues']
        output_path = data.get('filepath', './plot2.png')  # 默认保存路径

        # 调整图像尺寸和DPI
        fig, ax = plt.subplots(figsize=(6, 4), dpi=150)
        bar_width = 1
        plt.bar(categories2, yCountValues2, color='blue', edgecolor='black', width=bar_width)

        plt.xlabel('区间')
        plt.ylabel('计数')
        plt.title('yCountValues的条形图')

        # 计算y轴的最大值，并设置y轴的范围
        max_y = max(yCountValues2)
        step = 500  # 设置step的值
        y_max_limit = ((max_y // step) + 1) * step

        # 计算纵轴刻度值
        num_yticks = 6
        y_step = y_max_limit / (num_yticks - 1)
        yticks = [i * y_step for i in range(num_yticks)]
        plt.ylim(0, y_max_limit)
        plt.yticks(yticks)

        for i, v in enumerate(yCountValues2):
            plt.text(i, v + 2, str(v), ha='center', va='bottom')

        description = (
            f"平均值: {statisticValues['平均值']:.2f}   "
            f"标准差: {statisticValues['标准差']:.2f}     "
            f"方差: {statisticValues['方差']:.2f}\n        "
            f"总和: {statisticValues['总和']:.2f}  "
            f"计数: {statisticValues['计数']:.2f}  "
            f"平方和: {statisticValues['平方和']:.2f}"
        )

        plt.text(0.4, -0.15, description, ha='center', va='top', transform=plt.gca().transAxes, fontsize=10, wrap=True, family='SimHei')

        plt.subplots_adjust(top=0.9)

        plt.tight_layout()

        # 保存图片到指定路径
        plt.savefig(output_path, format='png', bbox_inches='tight')

        return jsonify({"status": "success", "filepath": output_path})
    except KeyError as e:
        return jsonify({"error": f"Missing key: {str(e)}"}), 400


@app.route('/plot2', methods=['POST'])
def plot2():
    data = request.json
    try:
        yCountValues2 = data['yCountValues2']
        categories2 = data['categories2']
        statisticValues = data['statisticValues']
        output_path = data.get('filepath', './plot2.png')  # 默认保存路径

        # 调整图像尺寸和DPI
        fig, ax = plt.subplots(figsize=(6, 4), dpi=150)
        bar_width = 1
        plt.bar(categories2, yCountValues2, color='blue', edgecolor='black', width=bar_width)

        plt.xlabel('区间')
        plt.ylabel('计数')
        plt.title('yCountValues的条形图')

        # 计算y轴的最大值，并设置y轴的范围
        max_y = max(yCountValues2)
        step = 500  # 设置step的值
        y_max_limit = ((max_y // step) + 1) * step
        plt.ylim(0, y_max_limit)

        for i, v in enumerate(yCountValues2):
            plt.text(i, v + 2, str(v), ha='center', va='bottom')

        description = (
            f"平均值: {statisticValues['平均值']:.2f}   "
            f"标准差: {statisticValues['标准差']:.2f}     "
            f"方差: {statisticValues['方差']:.2f}\n        "
            f"总和: {statisticValues['总和']:.2f}  "
            f"计数: {statisticValues['计数']:.2f}  "
            f"平方和: {statisticValues['平方和']:.2f}"
        )

        plt.text(0.4, -0.15, description, ha='center', va='top', transform=plt.gca().transAxes, fontsize=10, wrap=True, family='SimHei')

        plt.subplots_adjust(top=0.9)

        plt.tight_layout()

        # 保存图片到指定路径
        plt.savefig(output_path, format='png', bbox_inches='tight')

        return jsonify({"status": "success", "filepath": output_path})
    except KeyError as e:
        return jsonify({"error": f"Missing key: {str(e)}"}), 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5027, use_reloader=False, use_debugger=False)
