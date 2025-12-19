import threading

# globalne słowniki do zapisywania mnożników i pomnożonych wartości
mki = {}
nki = {}

# pomocnicze funkcje, używane w programie w części "praktycznej"

# odejmuję 1 od indeksów ze względu na indeksowanie od 0
# zadanie A
def A(M, i, k):
    mki[f"{k}_{i}"] = M[k - 1][i - 1] / M[i - 1][i - 1]

# zadanie B
def B(M, i, j, k):
    nki[f"{k}_{i}_{j}"] = M[i - 1][j - 1] * mki[f"{k}_{i}"]

# zadanie C
def C(M, i, j, k):
    M[k - 1][j - 1] -= nki[f"{k}_{i}_{j}"]

# współbieżne wykonanie eliminacji Gaussa 
def parallel_gauss(matrix, fnf):
    # działam na kopii macierzy
    M = [[el for el in row] for row in matrix]

    for foata_class in fnf:
        thread_list = []

        # dla każdej operacji tworzę wątek
        for task in foata_class:
            if task["oper"] == "A":
                thread_list.append(threading.Thread(target = A(M, task["i"], task["k"])))
            elif task["oper"] == "B":
                thread_list.append(threading.Thread(target = B(M, task["i"], task["j"], task["k"])))
            else:
                thread_list.append(threading.Thread(target = C(M, task["i"], task["j"], task["k"])))

        # startowanie wątków
        for thread in thread_list:
            thread.start()

        # czekanie na wszystkie wątki
        for thread in thread_list:
            thread.join()

    return M

# podstawianie wsteczne, sprowadzam macierz do postaci jednostkowej
def backward_substitution(eliminated_matrix, N):
    # tutaj zapisuję wyniki
    results = [0 for _ in range(N)]

    for i in range(N - 1, -1, -1):
        # przenoszenie na prawą stronę równania
        for j in range(i + 1, N):
            eliminated_matrix[i][N] -= eliminated_matrix[i][j] * results[j]
            eliminated_matrix[i][j] = 0.

        # obliczenie xi
        eliminated_matrix[i][N] /= eliminated_matrix[i][i]
        results[i] = eliminated_matrix[i][N]
        
        eliminated_matrix[i][i] = 1.

    return results