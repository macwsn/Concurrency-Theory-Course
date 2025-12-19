import networkx as nx

# pomocnicze funkcje, używane w programie w części "teoretycznej"

#przetworzenie danych z pliku
def process_file(input_file):
    # oczekiwana składnia taka, jak opisana w poleceniu
    text = input_file.read()
    lines = text.split("\n")

    # pierwszy wiersz zawiera rozmiar macierzy
    N = int(lines[0])

    matrix = [[None for _ in range(N)] for _ in range(N)]
    column = [[None] for _ in range(N)]

    for i in range(N):
        row_elems = lines[i + 1].split(" ")

        for j in range(N):
            # element macierzy, typu float
            matrix[i][j] = float(row_elems[j])

    column_elems = lines[N + 1].split(" ")

    for i in range(N):
        # elementy transponowanej kolumny, typu float
        column[i][0] = float(column_elems[i])
    
    return N, matrix, column

# przedstawienie rodzaju operacji wraz z indeksami do wypisania
def print_op(op):
    op_string = ""

    if op["oper"] == "A":
        op_string = f"A_({op["i"]},{op["k"]})"
    else:
        op_string = f"{op["oper"]}_({op["i"]},{op["j"]},{op["k"]})"

    return op_string

# wyznaczenie alfabetu
def get_alphabet(N):
    alphabet = []
    line_breaks = [] # zabieg stylistyczny

    for i in range(1, N):
        for k in range(i + 1, N + 1):
            # znalezienie mnożnika - operacja A
            alphabet.append({ "oper" : "A", "i" : i, "k" : k })

            for j in range(i, N + 2):
                # pomnożenie elementu przez mnożnik - operacja B
                alphabet.append({ "oper" : "B", "i" : i, "j" : j, "k" : k })

                # odjęcie elementu - operacja C
                alphabet.append({ "oper" : "C", "i" : i, "j" : j, "k" : k })

            if i < N - 1:
                if line_breaks:
                    line_breaks.append(line_breaks[-1] + 2 * (N + 2 - i) + 1)
                else:
                    line_breaks.append(2 * (N + 2 - i))
    
    return alphabet, line_breaks

# wyznaczenie relacji zależności
def get_dependent_transactions(N):
    dependent = []
    line_breaks = [] # zabieg stylistyczny

    for i in range(1, N):
        for k in range(i + 1, N + 1):
            opA = { "oper" : "A", "i" : i, "k" : k }

            # zależności B od A w danym wierszu
            for j in range(i, N + 2):
                opB = { "oper" : "B", "i" : i, "j" : j, "k" : k }
                dependent.append((opA, opB))

            if line_breaks:
                line_breaks.append(line_breaks[-1] + N + 2 - i)
            else:
                line_breaks.append(N + 1 - i)

            # zależności C od B w danym wierszu
            for j in range(i, N + 2):
                opB = { "oper" : "B", "i" : i, "j" : j, "k" : k }
                opC = { "oper" : "C", "i" : i, "j" : j, "k" : k }
                dependent.append((opB, opC))

            line_breaks.append(line_breaks[-1] + N + 2 - i)

    for i in range(2, N):
        # zależności A od C między wierszami
        for k in range(i + 1, N + 1):
            opC1 = { "oper" : "C", "i" : i - 1, "j" : i, "k" : i }
            opC2 = { "oper" : "C", "i" : i - 1, "j" : i, "k" : k }
            opA = { "oper" : "A", "i" : i, "k" : k }
            dependent.append((opC1, opA))
            dependent.append((opC2, opA))

        line_breaks.append(line_breaks[-1] + N + 1 - i)

        # zależności B od C między wierszami
        for k in range(i + 1, N + 1):
            for j in range(i + 1, N + 2):
                opC = { "oper" : "C", "i" : i - 1, "j" : j, "k" : i }
                opB = { "oper" : "B", "i" : i, "j" : j, "k" : k }
                dependent.append((opC, opB))

        line_breaks.append(line_breaks[-1] + N + 1 - i)

        # zależności C od C między wierszami
        for k in range(i + 1, N + 1):
            for j in range(i + 1, N + 2):
                opC1 = { "oper" : "C", "i" : i - 1, "j" : j, "k" : k }
                opC2 = { "oper" : "C", "i" : i, "j" : j, "k" : k }
                dependent.append((opC1, opC2))

        if i < N - 1:
            line_breaks.append(line_breaks[-1] + N + 1 - i)
   
    return dependent, line_breaks

# wyznaczenie postaci normalnej Foaty
def get_fnf(N):
    fnf = []

    for i in range(1, N):
        # nowa klasa Foaty
        fnf.append([])

        for k in range(i + 1, N + 1):
            fnf[-1].append({ "oper" : "A", "i" : i, "k" : k })

        # nowa klasa Foaty
        fnf.append([])

        for k in range(i + 1, N + 1):
            for j in range(i, N + 2):
                fnf[-1].append({ "oper" : "B", "i" : i, "j" : j, "k" : k })

        # nowa klasa Foaty
        fnf.append([])

        for k in range(i + 1, N + 1):
            for j in range(i, N + 2):
                fnf[-1].append({ "oper" : "C", "i" : i, "j" : j, "k" : k })

    return fnf

# stworzenie grafu i ustalenie pozycji oraz kolorowania wierzchołków
def prepare_graph_to_draw(dependent, fnf):
    # graf skierowany
    G = nx.DiGraph()

    # możliwe kolory wierzchołków
    node_colors = ["lime", "orange", "cyan", "pink"]

    max_len = max(len(foata_class) for foata_class in fnf)

    # dodawanie wierzchołków i ustalanie ich pozycji oraz kolorów
    positions = {}
    colors = []

    for i in range(len(fnf)):
        shift_pos = max_len / len(fnf[i])
        base_pos = shift_pos / 2

        for j in range(len(fnf[i])):
            node = print_op(fnf[i][j])
            G.add_node(node)
            positions[node] = [base_pos + j * shift_pos, 1 - i]
            colors.append(node_colors[i % 4])

    # dodawanie krawędzi
    for dep in dependent:
        dep1, dep2 = dep
        node1 = print_op(dep1)
        node2 = print_op(dep2)
        G.add_edge(node1, node2)

    return G, positions, colors