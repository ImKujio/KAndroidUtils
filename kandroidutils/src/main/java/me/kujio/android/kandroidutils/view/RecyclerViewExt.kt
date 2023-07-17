package me.kujio.android.kandroidutils.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


@BindingAdapter("simpleAdapter")
fun setSimpleAdapter(
    recyclerView: RecyclerView, simpleAdapter: SimpleRecyclerAdapter?
) {
    if (simpleAdapter == null) return
    recyclerView.recycledViewPool.setMaxRecycledViews(0, 8)
    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context).apply {
        orientation = LinearLayoutManager.VERTICAL
    }
    recyclerView.adapter = simpleAdapter
}

@BindingAdapter("simpleAdapter", "poolSize")
fun setSimpleAdapter(
    recyclerView: RecyclerView, simpleAdapter: SimpleRecyclerAdapter?, poolSize: Int = 8
) {
    if (simpleAdapter == null) return
    recyclerView.recycledViewPool.setMaxRecycledViews(0, poolSize)
    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context).apply {
        orientation = LinearLayoutManager.VERTICAL
    }
    recyclerView.adapter = simpleAdapter
}

@BindingAdapter("simpleAdapter", "types", "poolSize")
fun setSimpleAdapter(
    recyclerView: RecyclerView, simpleAdapter: SimpleRecyclerAdapter?, types: Int = 1, poolSize: Int = 10
) {
    if (simpleAdapter == null) return
    repeat(types) { i ->
        recyclerView.recycledViewPool.setMaxRecycledViews(i, poolSize)
    }
    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context).apply {
        orientation = LinearLayoutManager.VERTICAL
    }
    recyclerView.adapter = simpleAdapter
}

class SimpleRecyclerAdapter(
    private val resId: (pos: Int) -> Int,
    private val count: () -> Int,
    private val bindData: (adapter: SimpleRecyclerAdapter, binding: ViewDataBinding, pos: Int) -> Unit
) : RecyclerView.Adapter<SimpleRecyclerAdapter.SimpleViewHolder>() {
    lateinit var ctx: Context
    private val resIds = ArrayList<Int>()

    class SimpleViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        ctx = recyclerView.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        return SimpleViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), resIds[viewType], parent, false
            )
        )
    }

    override fun getItemViewType(position: Int): Int {
        val id = resId(position)
        if (resIds.indexOf(id) == -1) resIds.add(id)
        return resIds.indexOf(id)
    }

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        bindData(this, holder.binding, position)
    }

    override fun getItemCount(): Int {
        return count()
    }
}