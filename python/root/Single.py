import os
import Util

import matplotlib.pyplot as pyplot
import pandas
from pandas import DataFrame


def main():
    project_names: list[str] = []
    for filename in os.listdir("csv"):
        if filename.endswith(".data.csv"):
            project_names.append(filename[:-len(".data.csv")])

    for project_name in project_names:
        print("Processing: {0}".format(project_name))
        handle_project(project_name)


def handle_project(project_name: str):
    data_frame: DataFrame = pandas.read_csv("csv/{}.data.csv".format(project_name), sep=',', index_col=[0])
    data_frame.drop(data_frame.tail(1).index, inplace=True)  # TODO: last row should not be there at all

    # Normalize index (time)
    start_time = data_frame.index.min()
    data_frame.index = (data_frame.index - start_time) / (60 * 60 * 24)

    linter_frame: DataFrame = pandas.read_csv("csv/{}.linter.csv".format(project_name), sep=',')

    for linter in Util.get_linters(linter_frame):
        linter_introduction_time: float = (linter[0] - start_time) / (60 * 60 * 24)
        linter_name: str = linter[1]

        # create sub plot
        number_of_metrics = len(data_frame.columns)
        number_of_plots_per_metric = 5
        axes = Util.create_sub_plot(number_of_metrics, number_of_plots_per_metric)

        # For each metric one row of plots
        for i, metric in enumerate(data_frame.columns):
            df: DataFrame = data_frame[[metric]].reset_index()
            model = Util.create_its_model(df, linter_introduction_time, metric)

            Util.plot(axes[i], linter_introduction_time, linter_name, metric, model)

        # Save complete plot
        pyplot.tight_layout()
        os.makedirs("plots", exist_ok=True)
        pyplot.savefig("plots/{0}.{1}.png".format(project_name, linter_name), dpi=300)
        pyplot.close()


if __name__ == "__main__":
    main()
    #handle_project("AChep.AcDisplay")
