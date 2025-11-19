import math
import os

import matplotlib.pyplot as pyplot
import pandas
import statsmodels.api as statsmodels
from matplotlib.axes import Axes
from pandas import DataFrame
from statsmodels.regression.linear_model import RegressionResults


def main():
    project_names: list[str] = []
    for filename in os.listdir("csv"):
        if filename.endswith(".data.csv"):
            project_names.append(filename[:-len(".data.csv")])
        elif filename.endswith(".linter.csv"):
            project_names.append(filename[:-len(".linter.csv")])

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

        columns = data_frame.columns
        num_cols = len(columns)

        # Choose grid size (square-ish)
        cols = 4
        rows = math.ceil(num_cols / cols)
        fig, axes = pyplot.subplots(rows, cols, figsize=(8 * cols, 5 * rows))
        axes = axes.flatten() if num_cols > 1 else [axes]

        index: int = 0
        # Plot each column in its own subplot
        for i, metric in enumerate(columns):
            index = index + 1
            df: DataFrame = data_frame[[metric]].reset_index()
            model = create_its_model(df, linter_introduction_time, metric)
            plot(axes[i], df, linter_introduction_time, linter_name, metric, model)

        # Remove empty plots
        for j in range(index, len(axes)):
            axes[j].set_visible(False)

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
    ax.set_title(metric)
    ax.legend(loc='upper left')


def create_its_model(data: DataFrame, linter_introduction_time: float, metric: str) -> RegressionResults:
    # Create two new columns
    data['intervention'] = (data['time'] >= linter_introduction_time).astype(int)
    data['time_after_intervention'] = (data['time'] - linter_introduction_time) * data['intervention']

    dependant_variable = data[metric]
    independent_variables = statsmodels.add_constant(data[['time', 'intervention', 'time_after_intervention']])

    # print(data.to_string())
    # print(data[metric].describe())

    # Fit the model
    model: RegressionResults = statsmodels.OLS(dependant_variable, independent_variables).fit()

    print(model.summary())

    return model


if __name__ == "__main__":
    main()
    # handle_project("Activiti.Activiti")
