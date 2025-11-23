import os

import matplotlib.pyplot as pyplot
import pandas
import statsmodels.api as statsmodels
from pandas import DataFrame

import Util


def main():
    project_names: list[str] = []
    for filename in os.listdir("csv"):
        if filename.endswith(".data.csv"):
            project_names.append(filename[:-len(".data.csv")])

    # Collect data from csvs
    # index: linter, value: list of dataframes with introduction dates
    print("Step 1: read data")
    all_data: dict[str, list[tuple[float, DataFrame]]] = {"CHECKSTYLE": [], "PMD": [], "SPOTBUGS": []}
    for project_name in project_names:

        linter_frame: DataFrame = pandas.read_csv("csv/{}.linter.csv".format(project_name), sep=',')
        for linter in Util.get_linters(linter_frame):
            data_frame: DataFrame = pandas.read_csv("csv/{}.data.csv".format(project_name), sep=',')
            data_frame.drop(data_frame.tail(1).index, inplace=True)

            linter_introduction_time: float = linter[0]
            linter_name: str = linter[1]

            data_frame["time"] = (data_frame["time"] - linter_introduction_time) / (60 * 60 * 24)
            data_frame['intervention'] = (data_frame['time'] >= 0).astype(int)
            data_frame['t_pre'] = data_frame['time'] * (1 - data_frame['intervention'])
            data_frame['t_post'] = data_frame['time'] * data_frame['intervention']
            data_frame = data_frame[(data_frame["time"] >= -730) & (data_frame["time"] <= 730)]
            all_data[linter_name].append((linter_introduction_time, data_frame))

    # Concat data
    print("Step 2: concatenate data")
    data_frames_per_linter: dict[str, DataFrame] = {}
    for linter_name in all_data:
        print("   - {0} data points for {1}".format(len(all_data[linter_name]), linter_name))
        df = pandas.DataFrame()
        for introduction_time, data_frame in all_data[linter_name]:
            df = pandas.concat([df, data_frame], axis=0)

        data_frames_per_linter[linter_name] = df

    # Create models and plots
    print("Step 3: create models and plots")
    for linter in data_frames_per_linter:
        print("   - starting with {0} data".format(linter))
        columns = pandas.read_csv("csv/{}.data.csv".format(project_names[0]), sep=',', index_col=[0]).columns
        data_frame = data_frames_per_linter[linter].sort_values(by='time')

        metrics_and_models = {}
        print("      * creating models")
        for index, metric in enumerate(columns):
            data = data_frame[["time", metric, "intervention", "t_pre", "t_post"]].dropna()

            # remove outliers
            Q1 = data[metric].quantile(0.25)
            Q3 = data[metric].quantile(0.75)
            IQR = Q3 - Q1

            lower_bound = Q1 - 1.5 * IQR
            upper_bound = Q3 + 1.5 * IQR

            data_no_outliers = data[(data[metric] >= lower_bound) & (data[metric] <= upper_bound)]

            # create model
            formula = '{0} ~ t_pre + t_post + intervention + 1'.format(metric)

            model = statsmodels.OLS.from_formula(formula, data=data_no_outliers).fit(
                cov_type='HAC',
                cov_kwds={'maxlags': 3}
            )

            metrics_and_models[metric] = model

        # plot
        print("      * creating plots")
        Util.plot(0, linter, metrics_and_models)

        pyplot.tight_layout()
        os.makedirs("plots.all", exist_ok=True)
        pyplot.savefig("plots.all/{0}.png".format(linter), dpi=300)
        pyplot.close()

    print("Finished")


if __name__ == "__main__":
    main()
