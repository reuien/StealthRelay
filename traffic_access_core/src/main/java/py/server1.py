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

@app.route('/plot1', methods=['POST'])
def plot1():
    data = request.json
    try:
        yCountValues = data['yCountValues']
        categories = data['categories']
        statisticValues = data['statisticValues']
        xTimeValues = data['xTimeValues']
        yAveValues = data['yAveValues']
        average_value = data['average_value']
        output_path = data.get('filepath', './plot1.png')  # 默认保存路径

        # 创建输出目录（如果不存在）
        output_dir = os.path.dirname(output_path)
        if output_dir and not os.path.exists(output_dir):
            os.makedirs(output_dir)

        xTimeStrings = [datetime.fromtimestamp(x / 1000).strftime('%Y-%m-%d\n%H:%M:%S') for x in xTimeValues]

        # 调整图像尺寸和DPI
        fig, (ax2, ax1) = plt.subplots(2, 1, figsize=(8, 6), dpi=150)

        ax2.plot(xTimeStrings, yAveValues, marker='o', label='yAveValues')
        ax2.axhline(y=average_value, color='r', linestyle='--', label=f'average: {average_value:.2f}')
        ax2.set_xlabel('时间')
        ax2.set_ylabel('值')
        ax2.set_title('折线图与平均值')
        ax2.legend()
        ax2.set_yticks(range(40, 161, 20))

        num_labels =6
        data_length = len(xTimeStrings)
        interval = max(1, data_length // (num_labels ))

        # 生成x轴标签索引
        xtick_indices = list(range(0, data_length, interval))
        ax2.set_xticks(xtick_indices)
        ax2.set_xticklabels([xTimeStrings[i] for i in xtick_indices])

        ax1.bar(categories, yCountValues, color='lightblue', edgecolor='black', width=1)
        ax1.set_xlabel('区间')
        ax1.set_ylabel('计数')
        ax1.set_title('yCountValues的条形图')

        plt.subplots_adjust(top=0.9)

        for i, v in enumerate(yCountValues):
            ax1.text(i, v, str(v), ha='center', va='bottom')

        description = (
            f"平均值:{statisticValues['平均值']:.2f} "
            f"标准差:{statisticValues['标准差']:.2f} "
            f"方差: {statisticValues['方差']:.2f}\n "
            f"总和: {statisticValues['总和']:.2f} "
            f"计数: {statisticValues['计数']:.2f} "
            f"平方和:{statisticValues['平方和']:.2f} "
        )

        ax1.text(0.5, -0.3, description, transform=ax1.transAxes, ha='center', va='top', fontsize=15, family='SimHei')

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

        plt.text(0.4, -0.15, description, ha='center', va='top', transform=plt.gca().transAxes, fontsize=15, wrap=True, family='SimHei')

        plt.subplots_adjust(top=0.9)

        plt.tight_layout()

        # 保存图片到指定路径
        plt.savefig(output_path, format='png', bbox_inches='tight')

        return jsonify({"status": "success", "filepath": output_path})
    except KeyError as e:
        return jsonify({"error": f"Missing key: {str(e)}"}), 400


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5024, use_reloader=False, use_debugger=False)
