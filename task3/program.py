import sys
import networkx as nx
import matplotlib.pyplot as plt

from theo_utils import process_file, print_op
from theo_utils import get_alphabet, get_dependent_transactions, get_fnf
from theo_utils import prepare_graph_to_draw 

from gauss_utils import parallel_gauss, backward_substitution

if __name__ == '__main__':
    # sprawdzenie poprawności uruchomienia programu
    if len(sys.argv) != 2:
        print("Nieprawidłowa liczba argumentów uruchomienia programu!")
        print("Oczekiwana postać uruchomienia programu:")
        print("py .\\program.py <nazwa pliku z danymi wejściowymi>")
        sys.exit(0)

    # próba otwarcia pliku podanego jako argument
    try:
        input_filename = sys.argv[1]
        input_file = open(input_filename, encoding = "utf8")
    except IOError:
        print(f"Nie udało się otworzyć pliku z danymi wejściowymi: {input_filename}")
        sys.exit(0)
    
    # przetworzenie danych z pliku
    N, matrix, column = process_file(input_file)

    # macierz uzupełniona
    complemented_matrix = [row + elem for row, elem in zip(matrix, column)]

    # alfabet w sensie teorii śladów, zgodnie z przyjętymi założeniami
    alphabet, line_breaks = get_alphabet(N)

    print("\nAlfabet:\nΣ = {", end = "")
    for i in range(len(alphabet)):
        print(print_op(alphabet[i]), end = "")
        if i < len(alphabet) - 1: print(",", end = " ")
        if i in line_breaks: print("\n     ", end = "")
    print("}")

    # wyznaczenie relacji zależności, korzystając ze specyfiki algorytmu
    dependent, dep_line_breaks = get_dependent_transactions(N)

    print("\nRelacja zależności:\nD = sym{{", end = "")
    for i in range(len(dependent)):
        dep1, dep2 = dependent[i]
        dep1_str = print_op(dep1)
        dep2_str = print_op(dep2)

        print(f"({dep1_str},{dep2_str})", end = "")

        if i < len(dependent) - 1: 
            print(",", end = " ")
            if i in dep_line_breaks: print("\n         ", end = "")
    print("}^+} u I_Σ")

    # wyznaczenie śladu wykonania algorytmu
    print("\nŚlad wykonania algorytmu:\nw = ", end = "")
    for i in range(len(alphabet)):
        print(print_op(alphabet[i]), end = "")
        if i in line_breaks: print("\n    ", end = "")
    print()

    # wyznaczenie postaci normalnej Foaty, korzystając ze specyfiki algorytmu
    fnf = get_fnf(N)

    print("\nPostać normalna Foaty:\nFNF([w]) = ", end = "")
    for i in range(len(fnf)):
        print("[", end = "")
        for transaction in fnf[i]: print(print_op(transaction), end = "")
        print("]", end = "")
        if i % 2 == 1 and i < len(fnf) - 1: print("\n           ", end = "")
    print()

    # stworzenie i rysowanie grafu zależności Diekerta
    G, positions, colors = prepare_graph_to_draw(dependent, fnf)
    plt.figure(num = f"Graf zależności Diekerta wraz z kolorowaniem")
    nx.draw(G, positions, node_color = colors, node_size = 1200)
    nx.draw_networkx_labels(G, positions, font_size = 8)
    plt.show()

    # współbieżna eliminacja Gaussa
    eliminated_matrix = parallel_gauss(complemented_matrix, fnf)

    # obliczanie wyników - podstawianie wsteczne
    results = backward_substitution(eliminated_matrix, N)

    # macierz uzupełniona po rozwiązaniu układu
    print("\nMacierz po rozwiązaniu układu:")
    print(*eliminated_matrix, sep = "\n")

    # wyświetlanie wyników
    print("\nTransponowany wektor rozwiązań:", results)