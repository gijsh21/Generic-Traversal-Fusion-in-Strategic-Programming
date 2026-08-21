import random

def make_n(n):
  if n <= 0:
    return "Z()"
  else:
    return "S(" + make_n(n - 1) + ")"

def generate_n(mx):
  return make_n(random.randint(0, mx))

def generate(d, n_max):
  if d <= 0 or n_max <= 0:
    return "Null()"
  else:
    subtree1 = generate(d - 1, n_max)
    subtree2 = generate(d - 1, n_max)
    n1 = generate_n(n_max)
    n2 = generate_n(n_max)
    n3 = generate_n(n_max)
    return "E(" + subtree1 + ", " + subtree2 + ", " + n1 + ", " + n2 + ", " + n3 + ")"

def main():
  #d = int(input("Tree depth: "))
  d = int(input())
  n_max = int(input())
  #print("d = " + str(d) + " (n = " + str(2 ** d - 1) + ")")
  print(generate(d, n_max))

if __name__ == "__main__":
  main()
