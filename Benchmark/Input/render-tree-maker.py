import random
import sys

def make_page_list(page, n):
  res = ""
  for i in range(n - 1):
    res += "PageListInner(" + page + ", "
  res += "PageListEnd(" + page
  for i in range(n):
    res += ")"
  return res
    

  if n == 1:
    return "PageListEnd(" + page + ")"
  else:
    return "PageListInner(" + page + ", " + make_page_list(page, n - 1) + ")"

def generate(p):
  if p <= 0:
    return """Document(FontInfo(-1, -1, -1),
	PageListEnd(
		Page(
			AllData(-1, -1, -1, -1, -1.0, Abs(), FontInfo(-1, -1, -1), -1),
			HCL(-1, -1, -1,
				HorizontalContainer(
					AllData(-1, -1, -1, -1, -1.0, Abs(), FontInfo(-1, -1, -1), -1),
					EL(-1, -1, -1,
						Element(AllData(-1, -1, -1, -1, -1.0, Abs(), FontInfo(-1, -1, -1), -1), TextBox("Hello World!")), ElementListEnd())
				),
				HorizontalContainerListEnd()
			)
		)
	)
)"""
  else:
    page = """
    Page(AllData(-1, -1, -1, -1, -1.0, Flex(), FontInfo(-1, -1, -1), -1), 
	HCL(-1, -1, -1,
		HorizontalContainer(
			AllData(-1, -1, -1, -1, -1.0, Flex(), FontInfo(-1, -1, -1), -1),
			EL(-1, -1, -1,
				Element(AllData(-1, -1, -1, 50, -1.0, Abs(), FontInfo(-1, -1, -1), -1),
					List(
						ListItemInner("A", FontInfo(-1, -1, -1), -1, ListItemInner("B", FontInfo(-1, -1, -1), -1, ListItemEnd("C", FontInfo(-1, -1, -1), -1))), 3, 3
					)
				),
			ElementListInner(EL(-1, -1, -1,
				Element(AllData(-1, -1, -1, 150, -1.0, Abs(), FontInfo(-1, -1, -1), -1),
					VerticalContainer(
					HCL(-1, -1, -1,
						HorizontalContainer(AllData(-1, -1, -1, -1, 1.0, Rel(), FontInfo(-1, -1, -1), -1),
							EL(-1, -1, -1,
								Element(AllData(-1, -1, -1, -1, 1.0, Rel(), FontInfo(-1, -1, -1), -1), TextBox("Hello World!")), ElementListEnd()
							)
						),
					HorizontalContainerListInner(HCL(-1, -1, -1,
						HorizontalContainer(AllData(-1, -1, -1, -1, 0.75, Rel(), FontInfo(-1, -1, -1), -1),
							EL(-1, -1, -1,
								Element(AllData(-1, -1, -1, -1, 1.0, Rel(), FontInfo(-1, -1, -1), -1), Image("image.jpg", 100, 200, "")), ElementListEnd()
							)
						),
						HorizontalContainerListEnd()
					))
					)
				)),
				ElementListEnd())
			)
			)
		),
	HorizontalContainerListInner(HCL(-1, -1, -1,
		HorizontalContainer(
			AllData(-1, -1, -1, -1, 1.0, Rel(), FontInfo(-1, -1, -1), -1),
			EL(-1, -1, -1,
				Element(AllData(-1, -1, -1, -1, 1.0, Rel(), FontInfo(-1, -1, -1), -1), TextBox("Hello World!")),
				ElementListEnd()
			)
		), HorizontalContainerListEnd())
	)
	)
)
    """
    res = "Document(FontInfo(1, 1, 1), " + make_page_list(page, p) + ")"
    return res

def main():
  sys.setrecursionlimit(1500)
  n_pages = int(input())
  print(generate(n_pages))

if __name__ == "__main__":
  main()
