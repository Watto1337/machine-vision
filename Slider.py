from pygame import draw, font
font.init()

class Slider():
    sensitivity = 10
    text = font.SysFont("timesnewroman", 15)

    def __init__(self, val1, val2, width, pos, colour, name, array = None):
        if val1 < val2:
            self.min = val1
            self.max = val2
            self.reversed = False

        elif val1 > val2:
            self.min = val2
            self.max = val1
            self.reversed = True

        self.n = (val1 + val2) / 2
        self.val = self.n

        self.width = width
        self.pos = pos

        self.colour = colour

        self.name = Slider.text.render(name, True, colour, (0,0,0))

        if array != None: array.append(self)

    def set(self, val):
        self.val = val

        if self.reversed:
            self.n = self.min + self.max - self.val
        else:
            self.n = self.val

    def adjust(self, mousePos):
        if mousePos[0] > self.pos[0] - Slider.sensitivity and mousePos[0] < self.pos[0] + self.width + Slider.sensitivity and \
           mousePos[1] > self.pos[1] - Slider.sensitivity and mousePos[1] < self.pos[1] + Slider.sensitivity:
            self.n = max(self.min, min(self.max, (mousePos[0]-self.pos[0]) / self.width * (self.max-self.min) + self.min))

    def draw(self, mousePos, mouseDown, display):
        if mouseDown: self.adjust(mousePos)

        if self.reversed:
            self.val = self.min + self.max - self.n
        else:
            self.val = self.n

        draw.line(display, self.colour, self.pos, (self.pos[0] + self.width, self.pos[1]), 2)
        draw.circle(display, self.colour, (self.pos[0] + self.width * (self.n - self.min) / (self.max - self.min), self.pos[1]), 5)
        display.blit(self.name, (self.pos[0] + self.width + 10, self.pos[1] - 8))

        return self.val
