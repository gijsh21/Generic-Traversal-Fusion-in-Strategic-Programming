import json
import matplotlib.pyplot as plt
import numpy as np
import sys

def load_json(filename):
    with open(filename) as f:
            d = json.load(f)
            return d

# StackOverflow
def no_outliers(vs, m):
    d = np.abs(vs - np.median(vs))
    median_d = np.median(d)
    s = d/median_d if median_d else np.zeros(len(d))
    return vs[s<m]

def make_plot(data, benchmark_title, remove_outliers):
    cnt = 0
    yticks = []

    for benchmark, values in data.items():
        values = np.asarray(values)
        if remove_outliers:
            values = no_outliers(values, 2)
        plt.scatter(values, np.full_like(values, cnt, dtype=float), label=benchmark)

        mean = np.mean(values)
        std = np.std(values)
        print(benchmark + " "  + str(mean) + " " + str(std))
        plt.errorbar(mean, cnt, xerr=std,
                     fmt='D', color='black',
                     capsize=5, zorder=3,
                     alpha=0.5)

        yticks.append(benchmark)
        cnt += 1

    plt.xlabel("Runtime (ms)")
    plt.yticks(range(cnt), yticks)
    plt.title("Benchmark " + benchmark_title)
    plt.show()

def make_plot_multi(data, benchmark_title):
    xvals = np.asarray([0, 1, 2, 5, 10, 20])

    lblss = [[], []]
    meanss = [[], []]
    stdss = [[], []]

    for d in data:
        cnt = 0
        for benchmark, values in d.items():
            values = np.asarray(values)
            mean = np.mean(values)
            std = np.std(values)
            meanss[cnt].append(mean)
            stdss[cnt].append(std)
            lblss[cnt].append(benchmark)
            cnt += 1

    print(str(meanss[0]) + " " + str(stdss[0]))
    print(str(meanss[1]) + " " + str(stdss[1]))
    for i in range(len(meanss[0])):
        print(meanss[1][i] - meanss[0][i])

    plt.errorbar(xvals, meanss[0], yerr=stdss[0], label='Fused', linestyle='dotted', marker='x')
    plt.errorbar(xvals, meanss[1], yerr=stdss[1], label='Unfused', linestyle='dotted', marker='x')
    plt.xlabel("Additional time per traversal (us)")
    plt.ylabel("Total runtime (ms)")
    plt.title("Benchmark " + benchmark_title)
    plt.xticks(xvals, xvals)
    plt.legend()
    plt.show()

def main():
    fname = "jmh-result.json"
    bname = "Fusion Benchmark"
    remove_outliers = "n"
    if len(sys.argv) > 1:
      fname = sys.argv[1]
    if len(sys.argv) > 2:
      bname = sys.argv[2]
    if len(sys.argv) > 3:
        remove_outliers = sys.argv[3]

    benchmark_title = bname

    if fname == "multi":
        raw0 = load_json("jmh-result-str2-render-tree.json")
        raw1us = load_json("jmh-result-sleep-1us.json")
        raw2us = load_json("jmh-result-sleep-2us.json")
        raw5us = load_json("jmh-result-sleep-5us.json")
        raw10us = load_json("jmh-result-sleep-10us.json")
        raw20us = load_json("jmh-result-sleep-20us.json")
        raws = [raw0, raw1us, raw2us, raw5us, raw10us, raw20us]

        data = []
        for i in range(len(raws)):
            r = raws[i]
            data.append({})
            for benchmark in r:
                benchmark_name = benchmark["benchmark"].split(".")[-1]
                if benchmark_name not in ["renderTreeFused", "renderTreeUnfused"]:
                    continue
                benchmark_datapoints = benchmark["primaryMetric"]["rawData"][0]
                data[i][benchmark_name] = benchmark_datapoints
        make_plot_multi(data, benchmark_title)
    else:
        raw = load_json(fname)
        data = {}
        for benchmark in raw:
            benchmark_name = benchmark["benchmark"].split(".")[-1]
            if benchmark_name.endswith("Fused"):
                benchmark_name = "Fused"
            elif benchmark_name.endswith("Original"):
                benchmark_name = "Original"
            elif benchmark_name.endswith("Unfused"):
                benchmark_name = "Unfused"
            benchmark_datapoints = benchmark["primaryMetric"]["rawData"][0]
            data[benchmark_name] = benchmark_datapoints

        data_sorted = {}
        data_sorted["Fused"] = data["Fused"]
        data_sorted["Unfused"] = data["Unfused"]
        data_sorted["Original"] = data["Original"]

        make_plot(data_sorted, benchmark_title, remove_outliers == 'y')

if __name__ == "__main__":
    main()