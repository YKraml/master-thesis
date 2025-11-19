import math
import os

import matplotlib.pyplot as pyplot
import pandas
import statsmodels.api as statsmodels
from matplotlib.axes import Axes
from pandas import DataFrame


def main():
    os.makedirs("plots", exist_ok=True)

    project_name = "Alluxio.alluxio"
    data_frame = pandas.read_csv("csv/{}.data.csv".format(project_name), sep=',', index_col=[0])
    linter_frame = pandas.read_csv("csv/{}.linter.csv".format(project_name), sep=',')

    for linter in get_linters(linter_frame):
        linter_introduction_time: int = linter[0]
        linter_name: str = linter[1]

        columns = data_frame.columns
        num_cols = len(columns)

        # Choose grid size (square-ish)
        cols = 4
        rows = math.ceil(num_cols / cols)
        fig, axes = pyplot.subplots(rows, cols, figsize=(8 * cols, 5 * rows))
        axes = axes.flatten() if num_cols > 1 else [axes]

        # Plot each column in its own subplot
        for i, metric in enumerate(columns):
            df: DataFrame = data_frame[[metric]].reset_index()
            do_magic(df, linter_introduction_time, linter_name, metric, axes[i])

        # Remove empty plots
        for j in range(i + 1, len(axes)):
            axes[j].set_visible(False)

        # Save complete plot
        pyplot.tight_layout()
        pyplot.savefig("plots/{0}.{1}.png".format(project_name, linter_name), dpi=300)
        pyplot.close()


def get_linters(linter_frame: DataFrame) -> list[tuple[int, str]]:
    linters = []
    for idx, row in linter_frame.iterrows():
        time = row.iloc[0]
        linter = row.iloc[1]
        linters.append((time, linter))
    return linters


def do_magic(data: DataFrame, linter_introduction_time: int, linter_name: str, metric: str, ax: Axes):
    # Define the intervention time
    intervention_time = linter_introduction_time  # replace x with your actual time

    # Create the intervention indicator
    data['intervention'] = (data['time'] >= intervention_time).astype(int)

    # Create the time after intervention variable (0 before, time since intervention after)
    data['time_after_intervention'] = data['time'] - intervention_time
    data['time_after_intervention'] = data['time_after_intervention'] * data['intervention']

    # Prepare the regression variables
    X = statsmodels.add_constant(data[['time', 'intervention', 'time_after_intervention']])
    y = data[metric]

    # Fit the model
    model = statsmodels.OLS(y, X).fit()

    # print(model.summary())
    # print(df.to_string())

    # Plot observed data
    data.plot.scatter(x='time', y=metric, ax=ax, label='Observed')

    # Plot intervention line
    ax.axvline(x=intervention_time, linestyle='--', color='green', label="{0} introduced".format(linter_name))

    # Plot fitted regression line
    ax.plot(data['time'], model.fittedvalues, color='red', label='Fitted')

    # Plot title and legend
    ax.set_title(f'ITS for {metric}')
    # ax.legend(loc='upper left')


if __name__ == "__main__":
    main()
