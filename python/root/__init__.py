import math
import os

import matplotlib.pyplot as pyplot
import numpy
import pandas
import statsmodels.api as statsmodels
from matplotlib.axes import Axes
from pandas import DataFrame
from statsmodels.regression.linear_model import RegressionResults

from statsmodels.tsa.stattools import pacf
import statsmodels.api as sm
import statsmodels.formula.api as smf
import pandas as pd


def main():
    project_names: list[str] = []
    for filename in os.listdir("csv"):
        if filename.endswith(".data.csv"):
            project_names.append(filename[:-len(".data.csv")])

    # Remove duplicates
    unique_project_names: list[str] = sorted(list(set(project_names)))

    for project_name in unique_project_names:
        print("Processing: {0}".format(project_name))
        handle_project(project_name)


def handle_project(project_name: str):
    data_frame: DataFrame = pandas.read_csv("csv/{}.data.csv".format(project_name), sep=',', index_col=[0])
    data_frame.drop(data_frame.tail(1).index, inplace=True)  # TODO: last row should not be there at all

    # Normalize index (time)
    start_time = data_frame.index.min()
    data_frame.index = (data_frame.index - start_time) / (60 * 60 * 24)

    linter_frame: DataFrame = pandas.read_csv("csv/{}.linter.csv".format(project_name), sep=',')

    for linter in get_linters(linter_frame):
        linter_introduction_time: float = (linter[0] - start_time) / (60 * 60 * 24)
        linter_name: str = linter[1]

        num_cols = len(data_frame.columns)

        # Choose grid size
        cols = 5
        rows = num_cols
        fig, axes = pyplot.subplots(rows, cols, figsize=(5 * cols, 3 * rows))
        axes = axes.flatten() if num_cols > 1 else [axes]

        # For each metric one row of plots
        for i, metric in enumerate(data_frame.columns):
            df: DataFrame = data_frame[[metric]].reset_index()
            model = create_its_model(df, linter_introduction_time, metric)

            plot0 = axes[cols * i]
            plot0.hist(df[metric])
            plot0.set_title("{0} Distribution".format(metric))

            plot1 = axes[cols * i + 1]
            plot(plot1, df, linter_introduction_time, linter_name, metric, model)

            plot2 = axes[cols * i + 2]
            plot2.hist(numpy.array(model.fittedvalues) - numpy.array(df[metric]))
            plot2.set_title('Error Distribution')

            plot3 = axes[cols * i + 3]
            plot3.plot(numpy.array(model.fittedvalues) - numpy.array(df.dropna()[metric]))
            plot3.axhline(linestyle="--", color="green")
            plot3.set_title('Error over Time')

            plot4 = axes[cols * i + 4]
            sm.graphics.tsa.plot_pacf(numpy.array(model.fittedvalues) - numpy.array(df.dropna()[metric]), lags=40,
                                      ax=plot4)
            plot4.set_title("Partial Autocorrelation Function (PACF)")
            plot4.set_xlabel("Lag")
            plot4.set_ylabel("Partial Autocorrelation")

        # Save complete plot
        pyplot.tight_layout()
        os.makedirs("plots", exist_ok=True)
        pyplot.savefig("plots/{0}.{1}.png".format(project_name, linter_name), dpi=300)
        pyplot.close()


def get_linters(linter_frame: DataFrame) -> list[tuple[int, str]]:
    linters = []
    for idx, row in linter_frame.iterrows():
        time = row.iloc[0]
        linter = row.iloc[1]
        linters.append((time, linter))
    return linters


def plot(ax: Axes, data: DataFrame, linter_introduction_time: float, linter_name: str, metric: str, model):
    # Plot observed data
    data.plot.scatter(x='time', y=metric, ax=ax, label='Observed')

    # Plot intervention line
    ax.axvline(x=linter_introduction_time, linestyle='--', color='green', label="{0} introduced".format(linter_name))

    # Plot fitted regression line
    ax.plot(data['time'], model.fittedvalues, color='red', label='Fitted')

    # Plot title and legend
    ax.set_title("Observed data and fitted line")
    ax.legend(loc='upper left')


def create_its_model(data: DataFrame, linter_introduction_time: float, metric: str) -> RegressionResults:
    # Create three new columns
    # Looks like this after: index, time, averageFileSloc, intervention, t_pre, t_post
    data['intervention'] = (data['time'] >= linter_introduction_time).astype(int)
    data['t_pre'] = data['time'] * (1 - data['intervention'])
    data['t_post'] = data['time'] * data['intervention']

    formula = '{0} ~ t_pre + t_post + intervention + 1'.format(metric)

    # Fit the linear mixed-effects model
    model = sm.GLSAR.from_formula(formula, data=data, rho=1)
    model = model.iterative_fit(maxiter=10)

    print("")
    print (metric)
    # print(data)
    # print(data.to_string())
    # print(data[metric].describe())
    print(model.summary())

    return model


if __name__ == "__main__":
    # main()
    handle_project("AChep.AcDisplay")
