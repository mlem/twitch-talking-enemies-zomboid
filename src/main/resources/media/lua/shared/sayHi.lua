
local function NoOp(zombie)
end

Events.OnZombieUpdate.Add(NoOp)
Events.OnGameStart.Add(NoOp)
Events.OnAIStateChange.Add(NoOp)
Events.onMainScreenRender.Add(NoOp)
