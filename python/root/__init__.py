import math
from os import listdir

import matplotlib.pyplot as pyplot
import pandas


def plot_data_frame(data_frame, file_name):
    columns = data_frame.columns
    num_cols = len(columns)

    # Choose grid size (square-ish)
    cols = 4
    rows = math.ceil(num_cols / cols)

    fig, axes = pyplot.subplots(rows, cols, figsize=(8 * cols, 5 * rows))
    axes = axes.flatten() if num_cols > 1 else [axes]

    index_values = data_frame.index
    mid_index = index_values[int(len(index_values) / 2)]

    # Plot each column in its own subplot
    for i, column_name in enumerate(columns):
        ax = axes[i]

        # Plot the column
        data_frame[column_name].plot(ax=ax, title=column_name, xlabel="Days relative to linter introduction",
                                     ylabel=column_name)

        # Add vertical line in the middle with a name
        ax.axvline(
            x=mid_index,
            linestyle="--",
            label="Linter introduction"
        )

        # Show the legend
        ax.legend(loc="upper left")

    # Hide any unused subplots (if columns are odd number)
    for j in range(i + 1, len(axes)):
        axes[j].set_visible(False)

    pyplot.tight_layout()
    pyplot.savefig("plots/" + file_name + ".png")
    pyplot.close()


def create_data_frame(file_name):
    data_frame = pandas.read_csv("csv/" + file_name, sep=',', index_col=0)
    data_frame = data_frame.reset_index(drop=True)
    data_frame.index = range(-730, 731)  # [-730,...,0,...,730]
    return data_frame


print("Start Python Script")

files = listdir("csv")
for file in files:
    print("Create plot for: " + file)
    data_frame = create_data_frame(file)


    file = file.replace(".csv","")
    plot_data_frame(data_frame, file)

# createPlot("Activiti.Activiti.CHECKSTYLE.csv")

print("End Python Script")
